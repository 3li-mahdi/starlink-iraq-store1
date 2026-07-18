package com.starlinkiraq.store.service;

import com.starlinkiraq.store.entity.DigitalAsset;
import com.starlinkiraq.store.entity.Product;
import com.starlinkiraq.store.exception.InsufficientStockException;
import com.starlinkiraq.store.repository.DigitalAssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * يدير تخصيص محتوى المنتجات الرقمية (أكواد تفعيل، روابط تحميل) لطلبات الزبائن، بحيث لا يُسلَّم نفس الكود مرتين.
 */
@Service
@RequiredArgsConstructor
public class DigitalAssetService {

    private final DigitalAssetRepository digitalAssetRepository;

    /**
     * يخصّص عدداً من نسخ المحتوى الرقمي غير المستخدمة لمنتج معيّن، ويربطها بطلب محدد.
     *
     * @param product المنتج الرقمي المطلوب تسليمه
     * @param quantity عدد النسخ المطلوب تخصيصها
     * @param orderId معرّف الطلب المرتبط
     * @return قائمة المحتوى المُخصَّص (أكواد التفعيل أو الروابط)
     * تأثير جانبي: يعدّل isUsed و assignedOrderId لكل نسخة تم تخصيصها بقاعدة البيانات
     */
    @Transactional
    public List<String> assignAssets(Product product, int quantity, Long orderId) {
        List<String> assignedContent = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
            DigitalAsset asset = digitalAssetRepository.findFirstByProduct_IdAndIsUsedFalse(product.getId())
                    .orElseThrow(() -> new InsufficientStockException(
                            "لا يوجد مخزون كافٍ من المحتوى الرقمي للمنتج: " + product.getName()));
            asset.setUsed(true);
            asset.setAssignedOrderId(orderId);
            digitalAssetRepository.save(asset);
            assignedContent.add(asset.getDeliveryContent());
        }
        return assignedContent;
    }
}
