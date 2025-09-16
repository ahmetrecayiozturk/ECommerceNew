package com.ecommerce.sagaservice.infrastructure;

import com.ecommerce.sagaservice.application.saga.SagaStepHandler;
import com.ecommerce.sagaservice.application.service.SagaOrchestratorService;
import com.ecommerce.sagaservice.domain.aggregate.SagaState;
import com.ecommerce.sagaservice.infrastructure.repository.SagaStateRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SagaTimeoutScheduler {

    private final SagaStateRepository sagaStateRepository;
    private final SagaOrchestratorService sagaOrchestratorService;
    private final SagaStepHandler sagaStepHandler;

    public SagaTimeoutScheduler(SagaStateRepository sagaStateRepository, SagaOrchestratorService sagaOrchestratorService, SagaStepHandler sagaStepHandler) {
        this.sagaStateRepository = sagaStateRepository;
        this.sagaOrchestratorService = sagaOrchestratorService;
        this.sagaStepHandler = sagaStepHandler;
    }

    @Scheduled(fixedDelay = 60000)//Her 60 saniyede bir çalışacak şekilde ayalıyoruz, bu bize atıyorum ödeme bekliyorz o yüzden saga hala sagastate in_progress ise ve currentstep payment değilise ve 60 saniyeden fazla olduysa tekrar payment command'ı atacak
    public void checkTimeout(){
        System.out.println("SagaTimeoutScheduler: checkTimeout");
        List<SagaState> pendingSagas = sagaStateRepository.findByStatus("IN_PROGRESS");
        if(pendingSagas != null && !pendingSagas.isEmpty()){
            for(SagaState sagaState: pendingSagas){
                //Eğer sagastatenin durumu COMPENSATED veya COMPLETED ise bu saga'yı atlıyoruz çünkü zaten delivery edilmiş bitmiş ya da companse edilmiş sagalar bunlar
                if ("COMPENSATED".equals(sagaState.getStatus()) || "COMPLETED".equals(sagaState.getStatus())) {
                    continue;
                }
                if("ORDER".equals(sagaState.getCurrentStep())){
                    System.out.println("SagaTimeoutScheduler: PAYMENT step timeout for orderId " + sagaState.getOrderId());
                    sagaStepHandler.handlePaymentStep(sagaState, sagaState.getPayload());
                }
                if("PAYMENT".equals(sagaState.getCurrentStep())){
                    System.out.println("SagaTimeoutScheduler: PRODUCT step timeout for orderId " + sagaState.getOrderId());
                    sagaStepHandler.handleProductStep(sagaState, sagaState.getPayload());
                }
                if("PRODUCT".equals(sagaState.getCurrentStep())){
                    System.out.println("SagaTimeoutScheduler: DELIVERY step timeout for orderId " + sagaState.getOrderId());
                    sagaStepHandler.handleDeliveryStep(sagaState, sagaState.getPayload());
                }
            }
        }
    }
}