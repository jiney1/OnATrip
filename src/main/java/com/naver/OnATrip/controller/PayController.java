package com.naver.OnATrip.controller;

import com.naver.OnATrip.entity.pay.Item;
import com.naver.OnATrip.entity.pay.Pay;
import com.naver.OnATrip.entity.pay.PrePaymentEntity;
import com.naver.OnATrip.service.ItemService;
import com.naver.OnATrip.service.PayService;
import com.naver.OnATrip.web.dto.pay.ItemDto;
import com.naver.OnATrip.web.dto.pay.PayInfoDto;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.Payment;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
@RequiredArgsConstructor
public class PayController {

    private static final Logger logger = LoggerFactory.getLogger(PayController.class);

    private final PayService payService;
    private final ItemService itemService;


    /**
     * 아임포트 결제 검증 컨트롤러
     **/
    private IamportClient iamportClient;

    @Value("${imp.api.key}")    //application.yml에서 값을 불러와서 사용
    private String apiKey;

    @Value("${imp.api.secretKey")
    private String secretKey;

    @PostConstruct
    private void init() {
        this.iamportClient = new IamportClient(apiKey, secretKey);
    }

    //사전 검증
    @PostMapping("/pay/prepare")
    public void preparePayment(@RequestBody PrePaymentEntity request)
            throws IamportResponseException, IOException {
        log.info("Preparing payment: {}", request);
        payService.postPrepare(request);
        log.info("Payment prepared successfully for merchant_uid: {}", request.getMerchantUid());

    }

    @PostMapping("/payPage")
    public String getItemById(@RequestParam("item_id") int itemId, Model model) {
        Optional<Item> itemOptional = itemService.findById(itemId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            model.addAttribute("item", item);
            log.info("Item found: {}", item);
        }

        return "pay/payPage";
    }

    public Payment validatePayment(@RequestBody PayInfoDto request)
                    throws IamportResponseException, IOException{
        log.info("Validating payment: {}", request);
        return payService.validatePayment(request);
    }


    /** 프론트에서 받은 PG사 결괏값을 통해 아임포트 토큰 발행 **/
//    @PostMapping("/{imp_uid}")
//    public IamportResponse<Payment> paymentByImpUid(@PathVariable String imp_uid) throws IamportResponseException, IOException{
//        log.info("paymentByImpUid 진입");
//        return iamportClient.paymentByImpUid(imp_uid);
//    }


//    @PostMapping("/{imp_uid}")
//    public IamportResponse<Payment> paymentByImpUid(Model model, Locale locale,
//                                                    HttpSession httpSession, @PathVariable(value = "imp_uid") String imp_uid)
//            throws IamportResponseException, IOException {
//        return api.paymentByImpUid(imp_uid);
//    }


}
