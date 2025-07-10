package letsit_backend.Apply;

import letsit_backend.dto.apply.ApplicantProfileDto;
import letsit_backend.dto.apply.ApplyRequestDto;
import letsit_backend.dto.apply.ApplyResponseDto;
import letsit_backend.exception.ApplyErrorCode;
import letsit_backend.exception.CommonErrorCode;
import letsit_backend.model.Apply;
import letsit_backend.model.Member;
import letsit_backend.model.Post;
import letsit_backend.model.Profile;
import letsit_backend.repository.ApplyRepository;
import letsit_backend.repository.PostRepository;
import letsit_backend.repository.ProfileRepository;
import letsit_backend.service.ApplyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)     // Mockito 기반 단위테스트
public class ApplyServiceTest {

    private static final Logger log = LoggerFactory.getLogger(ApplyServiceTest.class);
    @Mock
    private PostRepository postRepository;

    @Mock
    private ApplyRepository applyRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ApplyService applyService;

    private Member mockMember;
    private Post mockPost;
    private ApplyRequestDto applyRequest;
    private Apply mockApply;

    @BeforeEach
    void setUp() {
        mockMember = Member.builder().userId(1L).build();
        mockPost = Post.builder()
                .postId(1L)
                .userId(mockMember)
                .recruitDueDate(LocalDate.now().plusDays(7)) // ← 모집 마감일 추가
                .deadline(Boolean.FALSE)
                .totalPersonnel(Post.TotalPersonnel.TWO) // 🔍 이 라인 추가
                .build();
        applyRequest = ApplyRequestDto.builder()
                .preferStack("Java")
                .desiredField("백엔드")
                .applyContent("content")
                .contact("contact@example.com")
                .build();
        mockApply = applyRequest.toEntity(mockPost, mockMember);
    }


    @Test
    void create_정상제출() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));
        when(applyRepository.findByPostId(mockPost)).thenReturn(Collections.emptyList());
        when(applyRepository.save(any(Apply.class))).thenReturn(mockApply);

        ApplyResponseDto response = applyService.create(1L, mockMember, applyRequest);

        assertThat(response.getUserId()).isEqualTo(mockMember.getUserId());
        assertThat(response.getPreferStack()).isEqualTo("Java");
        assertThat(response.getDesiredField()).isEqualTo("백엔드");
        verify(applyRepository).save(any(Apply.class));
    }

    @Test
    void create_기제출예외() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));
        when(applyRepository.findByPostId(mockPost)).thenReturn(Collections.singletonList(mockApply));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.create(1L, mockMember, applyRequest));
        log.error("exception occured in Apply: {}", exception.getMessage());
        assertThat(exception.getMessage()).contains(ApplyErrorCode.ALREADY_APPLIED.getMessage());
    }


    @Test
    void read_정상조회() {
        Apply apply = spy(mockApply);
        when(apply.getApplyId()).thenReturn(100L); // 가짜 applyId 설정

        when(applyRepository.findById(100L)).thenReturn(Optional.of(apply));

        // 테스트 대상: 지원자 본인이 조회
        ApplyResponseDto response = applyService.read(100L, mockMember);

        assertThat(response.getApplyId()).isEqualTo(100L);
        assertThat(response.getUserId()).isEqualTo(mockMember.getUserId());
        assertThat(response.getPreferStack()).isEqualTo(apply.getPreferStack());
        assertThat(response.getDesiredField()).isEqualTo(apply.getDesiredField());
    }

    @Test
    void read_지원자아님_예외() {
        Apply apply = spy(mockApply);
        Member postOwner = Member.builder().userId(2L).build(); // 게시자

        Post post = Post.builder().postId(1L).userId(postOwner).build();
        doReturn(post).when(apply).getPostId(); // 게시자 설정
        doReturn(Member.builder().userId(3L).build()).when(apply).getMember(); // 지원자 아님

        when(applyRepository.findById(100L)).thenReturn(Optional.of(apply));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.read(100L, mockMember)); // 지원자도 게시자도 아님

        assertThat(exception.getMessage()).contains(CommonErrorCode.FORBIDDEN.getMessage());
    }

    @Test
    void read_게시자아님_예외() {
        Apply apply = spy(mockApply);
        Member actualApplicant = mockMember;
        Member postOwner = Member.builder().userId(2L).build();

        Post post = Post.builder().postId(1L).userId(postOwner).build();
        doReturn(post).when(apply).getPostId();
        doReturn(actualApplicant).when(apply).getMember();

        when(applyRepository.findById(100L)).thenReturn(Optional.of(apply));

        Member otherMember = Member.builder().userId(999L).build(); // 게시자 아님
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.read(100L, otherMember)); // 지원자도 게시자도 아님

        assertThat(exception.getMessage()).contains(CommonErrorCode.FORBIDDEN.getMessage());
    }

    @Test
    void read_지원서없음_예외() {
        when(applyRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.read(999L, mockMember));

        assertThat(exception.getMessage()).contains(ApplyErrorCode.APPLICATION_NOT_FOUND.getMessage());
    }


    @Test
    void delete_정상처리() {
        Apply apply = spy(mockApply);

        when(applyRepository.findById(100L)).thenReturn(Optional.of(apply));

        applyService.delete(100L, mockMember);
        verify(applyRepository).delete(apply);
    }

    @Test
    void delete_지원서없음_예외() {
        when(applyRepository.findById(999L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.delete(999L, mockMember));

        assertThat(exception.getMessage()).contains(ApplyErrorCode.APPLICATION_NOT_FOUND.getMessage());
    }

    @Test
    void delete_권한없음_예외() {
        Apply apply = spy(mockApply);
        Member otherMember = Member.builder().userId(999L).build();
        doReturn(otherMember).when(apply).getMember();

        when(applyRepository.findById(100L)).thenReturn(Optional.of(apply));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.delete(100L, mockMember)); // 삭제 권한 없는 사용자

        assertThat(exception.getMessage()).contains(CommonErrorCode.FORBIDDEN.getMessage());
    }


    @Test
    void getPendingApplicantProfiles_정상조회() {
        Post post = mockPost;
        Apply pendingApply = spy(mockApply);
        doReturn(true).when(pendingApply).isNullYet();

        Profile profile = Profile.builder()
                .member(mockMember)
                .nickname("테스트유저")
                .name("김숙명")
                .profileImageUrl("image1")
                .build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(applyRepository.findByPostId(post)).thenReturn(List.of(pendingApply));
        when(profileRepository.findByMemberIn(List.of(mockMember))).thenReturn(List.of(profile)); // ✅ 실제 profile mock 반환

        List<ApplicantProfileDto> result = applyService.getPendingApplicantProfiles(1L, mockMember);

        assertThat(result).hasSize(1);
    }

    @Test
    void getPendingApplicantProfiles_0명조회() {
        Post post = mockPost;
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(applyRepository.findByPostId(post)).thenReturn(List.of()); // 지원서 없음
        when(profileRepository.findByMemberIn(List.of())).thenReturn(List.of()); // 프로필도 없음

        List<ApplicantProfileDto> result = applyService.getPendingApplicantProfiles(1L, mockMember);

        assertThat(result).isEmpty(); // 결과가 0명인지 확인
    }

    @Test
    void getPendingApplicantProfiles_구인글없음_예외() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.getPendingApplicantProfiles(999L, mockMember));

        assertThat(exception.getMessage()).contains("해당 구인글이 존재하지 않습니다.");
    }

    @Test
    void getPendingApplicantProfiles_권한없음_예외() {
        Post post = Post.builder()
                .postId(2L)
                .userId(Member.builder().userId(2L).build()) // 다른 사용자가 작성한 게시글
                .build();

        when(postRepository.findById(2L)).thenReturn(Optional.of(post));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.getPendingApplicantProfiles(2L, mockMember));

        assertThat(exception.getMessage()).contains(CommonErrorCode.FORBIDDEN.getMessage());
    }


    @Test
    void getApprovedApplicantProfiles_정상조회() {
        Post post = mockPost;

        // 두 명의 지원자
        Member member1 = Member.builder().userId(1L).build();
        Member member2 = Member.builder().userId(2L).build();

        Apply approvedApply1 = spy(mockApply);
        Apply approvedApply2 = spy(mockApply);

        doReturn(true).when(approvedApply1).isApproved();
        doReturn(true).when(approvedApply2).isApproved();
        doReturn(member1).when(approvedApply1).getMember();
        doReturn(member2).when(approvedApply2).getMember();

        Profile profile1 = Profile.builder()
                .member(member1)
                .nickname("유저1")
                .name("김숙명")
                .profileImageUrl("image1")
                .build();

        Profile profile2 = Profile.builder()
                .member(member2)
                .nickname("유저2")
                .name("이숙명")
                .profileImageUrl("image2")
                .build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(applyRepository.findByPostId(post)).thenReturn(List.of(approvedApply1, approvedApply2));
        when(profileRepository.findByMemberIn(List.of(member1, member2))).thenReturn(List.of(profile1, profile2));

        List<ApplicantProfileDto> result = applyService.getApprovedApplicantProfiles(1L, mockMember);

        assertThat(result).hasSize(2);
    }

    @Test
    void getApprovedApplicantProfiles_구인글없음_예외() {
        when(postRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.getApprovedApplicantProfiles(999L, mockMember));

        assertThat(exception.getMessage()).contains("해당 구인글이 존재하지 않습니다.");
    }

    @Test
    void getApprovedApplicantProfiles_권한없음_예외() {
        Post post = Post.builder()
                .postId(2L)
                .userId(Member.builder().userId(2L).build()) // 다른 사용자가 작성한 게시글
                .build();

        when(postRepository.findById(2L)).thenReturn(Optional.of(post));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.getApprovedApplicantProfiles(2L, mockMember));

        assertThat(exception.getMessage()).contains(CommonErrorCode.FORBIDDEN.getMessage());
    }

    @Test
    void approveApplicant_정상승인() {
        Post post = spy(mockPost);
        Apply apply = mockApply;

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(applyRepository.findById(100L)).thenReturn(Optional.of(apply));

        applyService.approveApplicant(1L, 100L, mockMember);

        verify(post).approval(apply);
        verify(applyRepository).save(apply);
    }

    @Test
    void approveApplicant_지원서없음_예외() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));
        when(applyRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.approveApplicant(1L, 100L, mockMember));

        assertThat(exception.getMessage()).contains(ApplyErrorCode.APPLICATION_NOT_FOUND.getMessage());
    }

    // 권한 없음 예외의 경우 프로필 취합 메서드들에서 사용하는 검증 메서드와 동일한 것을 호출하므로 생략함


    @Test
    void rejectApplicant_정상거절() {
        Post post = spy(mockPost);
        Apply apply = mockApply;

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(applyRepository.findById(100L)).thenReturn(Optional.of(apply));

        applyService.rejectApplicant(1L, 100L, mockMember);

        verify(post).reject(apply);
        verify(applyRepository).save(apply);
    }

    @Test
    void rejectApplicant_지원서없음_예외() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));
        when(applyRepository.findById(100L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                applyService.rejectApplicant(1L, 100L, mockMember));

        assertThat(exception.getMessage()).contains(ApplyErrorCode.APPLICATION_NOT_FOUND.getMessage());
    }

    // 권한 없음 예외의 경우 프로필 취합 메서드들에서 사용하는 검증 메서드와 동일한 것을 호출하므로 생략함
}
