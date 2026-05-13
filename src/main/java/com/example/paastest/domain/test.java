package com.example.paastest.domain;

public class test {
    // [V: Validate - 수신 및 검증]
// 1. Webhook 요청 수신 (페이로드 크기 1MB 제한)
// 2. event_type 필드가 "deal_won"인지 검증 -> 아니면 400 Bad Request 반환 후 로직 종료

// [T: Transform - 동적 매핑 및 예외 방어]
// 3. payload 데이터를 읽어 B(ERP) 시스템 규격에 맞춰 재조립 (Map 또는 ObjectNode 활용)
//    3-1. payload.deal_id -> erp_ref_id 로 매핑
//    3-2. payload.price -> amount_number 로 매핑 (String을 Integer로 캐스팅 시 NumberFormatException 방어)
//    3-3. payload.client_email -> contact 로 매핑 (값이 null이거나 필드가 없으면 "UNKNOWN" 할당)
//    3-4. sync_status -> "COMPLETED" 고정값 추가

// [C: Call - 외부 전송 및 타임아웃]
// 4. 재조립된 JSON 데이터를 ERP API로 POST 전송
//    4-1. Read Timeout을 5초로 엄격히 설정
//    4-2. 타임아웃 발생 시 504 Gateway Timeout 반환

// [R: Respond - 결과 응답]
// 5. ERP API 호출 성공(2xx 응답) 시, 최초 클라이언트에게 "계약 동기화 완료" 형태의 200 OK JSON 반환

}
