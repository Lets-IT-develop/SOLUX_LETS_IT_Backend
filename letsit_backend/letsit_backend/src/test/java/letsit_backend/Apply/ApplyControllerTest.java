package letsit_backend.Apply;

import com.fasterxml.jackson.databind.ObjectMapper;
import letsit_backend.controller.ApplyController;
import letsit_backend.dto.apply.ApplyRequestDto;
import letsit_backend.dto.apply.ApplyResponseDto;
import letsit_backend.dto.apply.ApplicantProfileDto;
import letsit_backend.jwt.JwtFilter;
import letsit_backend.model.Member;
import letsit_backend.service.ApplyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Collections;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = ApplyController.class,    // ApplyController만 테스트
        excludeAutoConfiguration = {                // JPA 자동 설정 제외
                org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration.class,
                org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration.class
        },                                          // JWT 필터 제외
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtFilter.class))
@ActiveProfiles("test")
public class ApplyControllerTest {
    @Autowired
    private MockMvc mockMvc;            // 실제 HTTP 요청 없이도 컨트롤러 테스트

    @Autowired
    private ObjectMapper objectMapper;  // JSON 직렬화/역직렬화 시 사용

    @MockBean
    private ApplyService applyService;  // ApplyService는 Mock 객체로 등록

    private Member mockUser;


    @BeforeEach
    void setUpSecurityContext() {
        mockUser = Member.builder()
                .userId(1L)
                .kakaoId(1L)
                .name("사용자_A")
                .build();
    } // 로그인된 사용자 설정

    @Test
    @DisplayName("지원서 제출 기능 테스트")
    void applyToPostTest() throws Exception {
        // Given
        Long postId = 1L;

        ApplyRequestDto applyRequest = ApplyRequestDto.builder()    // 요청 DTO 정의
                .postId(postId)
                .userId(mockUser.getUserId())
                .preferStack("Spring")
                .desiredField("백엔드")
                .applyContent("hello")
                .contact("world")
                .build();

        // When
        // 컨트롤러 작동 여부만 테스트하기 위해, 실제 DB나 서비스단 로직은 작동시키지 않고 정해진 응답을 반환하게 할 것.
        ApplyResponseDto responseDto = ApplyResponseDto.builder()   // 컨트롤러의 메서드가 applyService.create()를 호출하면 이 응답 DTO를 반환하게 할 것임
                .applyId(100L)
                .userId(mockUser.getUserId())
                .preferStack(applyRequest.getPreferStack())
                .desiredField(applyRequest.getDesiredField())
                .applyContent(applyRequest.getApplyContent())
                .contact(applyRequest.getContact())
                .applyCreateDate(new java.sql.Timestamp(System.currentTimeMillis())) // 현재 시각
                .build();

        // Then
        Mockito.when(applyService.create(Mockito.eq(postId), Mockito.any(Member.class), Mockito.any(ApplyRequestDto.class)))
                .thenReturn(responseDto);

        // When & Then
        // 가상의 HTTP 요청 실행
        mockMvc.perform(post("/apply/{postId}/write", postId)   // 테스트할 엔드포인트
                        .with(csrf())   // CSRF 보호 통과하기 위해 토큰 추가
                        .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, Collections.emptyList())))    // 인증된 사용자 토큰으로 요청
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(applyRequest)))    // 요청 DTO 객체를 직렬화
                .andDo(print())     // 요청 및 응답의 전체 내용을 출력
                .andExpect(status().isCreated())    // 201 Created여야 함
                .andExpect(jsonPath("$.message").value("성공"))
                .andExpect(jsonPath("$.data.applyId").value(100L))
                .andExpect(jsonPath("$.data.userId").value(mockUser.getUserId()))
                .andExpect(jsonPath("$.data.preferStack").value("Spring"))
                .andExpect(jsonPath("$.data.desiredField").value("백엔드"))
                .andExpect(jsonPath("$.data.applyContent").value("hello"))
                .andExpect(jsonPath("$.data.contact").value("world"));
    }

    @Test
    @DisplayName("지원서 조회 기능 테스트")
    void getApplyTest() throws Exception {
        // Given
        Long applyId = 100L;
        ApplyResponseDto responseDto = ApplyResponseDto.builder()
                .applyId(applyId)
                .userId(mockUser.getUserId())
                .preferStack("Spring")
                .desiredField("백엔드")
                .applyContent("hello")
                .contact("world")
                .applyCreateDate(new java.sql.Timestamp(System.currentTimeMillis()))
                .build();

        Mockito.when(applyService.read(Mockito.eq(applyId), Mockito.any(Member.class)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/apply/{applyId}", applyId)
                                .with(csrf())
                                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, Collections.emptyList())))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원서 보기"))
                .andExpect(jsonPath("$.data.applyId").value(applyId))
                .andExpect(jsonPath("$.data.userId").value(mockUser.getUserId()))
                .andExpect(jsonPath("$.data.preferStack").value("Spring"))
                .andExpect(jsonPath("$.data.desiredField").value("백엔드"))
                .andExpect(jsonPath("$.data.applyContent").value("hello"))
                .andExpect(jsonPath("$.data.contact").value("world"));
    }

    @Test
    @DisplayName("지원서 삭제 기능 테스트")
    void deleteApplyTest() throws Exception {
        // Given
        Long applyId = 100L;
        Mockito.doNothing().when(applyService).delete(Mockito.eq(applyId), Mockito.any(Member.class));

        // When & Then
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/apply/{applyId}/delete", applyId)
                                .with(csrf())
                                .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, Collections.emptyList())))
                )
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("지원서를 삭제하였습니다"));
    }

    @Test
    @DisplayName("지원자 리스트 조회 테스트")
    void getApplicantListTest() throws Exception {
        Long postId = 1L;
        List<ApplicantProfileDto> mockList = List.of(
                new ApplicantProfileDto(1L, "지원자1", "김숙명", "image0"),
                new ApplicantProfileDto(2L, "지원자2", "이명신", "image1")
        );

        Mockito.when(applyService.getPendingApplicantProfiles(Mockito.eq(postId), Mockito.any(Member.class)))
                .thenReturn(mockList);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/apply/{postId}/list", postId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, Collections.emptyList()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("지원자 리스트"))
                .andExpect(jsonPath("$.data[0].name").value("김숙명"))
                .andExpect(jsonPath("$.data[1].profileImage").value("image1"));
    }

    @Test
    @DisplayName("승인된 지원자 리스트 조회 테스트")
    void getApprovedApplicantListTest() throws Exception {
        Long postId = 1L;
        List<ApplicantProfileDto> mockList = List.of(
                new ApplicantProfileDto(3L, "지원자3", "박순헌", "Spring")
        );

        Mockito.when(applyService.getApprovedApplicantProfiles(Mockito.eq(postId), Mockito.any(Member.class)))
                .thenReturn(mockList);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/apply/{postId}/approvedlist", postId)
                        .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, Collections.emptyList()))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("승인된 지원자 리스트"))
                .andExpect(jsonPath("$.data[0].nickname").value("지원자3"));
    }

    @Test
    @DisplayName("지원자 승인 테스트")
    void approvalApplicantTest() throws Exception {
        Long postId = 1L;
        Long applyId = 100L;

        Mockito.doNothing().when(applyService).approveApplicant(postId, applyId, mockUser);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/apply/{postId}/list/{applyId}/approval", postId, applyId)
                        .with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, Collections.emptyList()))))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("지원서가 승인되었습니다."));
    }

    @Test
    @DisplayName("지원자 거절 테스트")
    void rejectionApplicantTest() throws Exception {
        Long postId = 1L;
        Long applyId = 100L;

        Mockito.doNothing().when(applyService).rejectApplicant(postId, applyId, mockUser);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/apply/{postId}/list/{applyId}/reject", postId, applyId)
                        .with(csrf())
                        .with(authentication(new UsernamePasswordAuthenticationToken(mockUser, null, Collections.emptyList()))))
                .andDo(print())
                .andExpect(status().isNoContent())
                .andExpect(jsonPath("$.message").value("지원서가 거절되었습니다."));
    }


}
