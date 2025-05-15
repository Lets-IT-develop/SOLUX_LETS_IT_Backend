package letsit_backend.service;

import letsit_backend.dto.team.*;
import letsit_backend.model.*;
import letsit_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.boot.model.naming.IllegalIdentifierException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class TeamService {

    private final PostRepository postRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamPostRepository teamPostRepository;
    private final ApplyRepository applyRepository;
    // private final TeamEvaluationRepository teamEvaluationRepository;
    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final ProfileService profileService;

    // TODO 생성자주입, setter사용지향, 빌드주입하기로 수정필요

    // 팀 게시판 생성
    @Transactional
    public Long creatTeam(Long postId, TeamCreateDto teamCreateDto) {

        // TODO userId 체크
        // TODO 이미 생성됐는지 검증

        // 팀게시판 생성로직
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalIdentifierException("게시글을 찾을수없음."));

        TeamPost newTeamPost = new TeamPost(
                post,
                teamCreateDto.getTeamName(),
                teamCreateDto.getNotionLink(),
                teamCreateDto.getGithubLink());

        teamPostRepository.save(newTeamPost); // 저장
        System.out.println("TeamId = " + newTeamPost.getTeamId());
        return newTeamPost.getTeamId();

    }

    // 팀멤버 생성
    // TODO 팀장은 currentUser로 만들지?
    @Transactional
    public void creatTeamMember(Long teamPostId) {

        // TODO 해당 게시글id의 apply승인된사람 정보 받아오기 -> 모집인원과 list길이일치하는지 검증필요
        // TODO 코드 리펙토링하기(teampost로 로직수행해도되는지, postid가져와서하는게 안전한지)
        // Post post = PostRepository.findById.orElseThrow(()->new RuntimeException("this is not found");
        Optional<TeamPost> teamPostOptional = teamPostRepository.findById(teamPostId);
        TeamPost teamPost = teamPostRepository.findById(teamPostId)
                .orElseThrow(() -> new RuntimeException("팀게시글을 찾을수없음"));


        Post post = teamPost.getPostId();              //post불러옴
        // 신청 승인된자 불러오기
        List<Apply> applies = applyRepository.findAllByPostId(post);
        for (Apply applie : applies) {
            if (applie.getApprovalStatus()) {                // 지원서 승인된사람불러옴.
                // 팀원목록 저장
                Member member = applie.getMember();   // member객체소환
                TeamMember teamMember = new TeamMember(teamPost, member, TeamMember.Role.Team_Member);
                teamMemberRepository.save(teamMember);
            }
        }

        // 팀장 저장
        Member postUserId = post.getUserId();
        TeamMember teamLeader = new TeamMember(teamPost, postUserId, TeamMember.Role.Team_Leader);
        teamMemberRepository.save(teamLeader);

    }

    // 팀정보 불러오기
    public TeamInfoResponseDto roadTeamInfo(Long teamId) {
        TeamPost teamPost = teamPostRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("team is not found"));

        List<TeamMember> teamMemberList = teamMemberRepository.findAllByTeamId(teamPost);
        // TODO 팀멤버리스트 없을시 예외처리 필요
        List<TeamMemberLoadInfoDto> teamInfoList = teamMemberList.stream()
                .map(teamMember -> {
                    // 프로필 url가져오기
                    Profile profile = profileRepository.findByMember(teamMember.getUserId());
                    //
                    TeamMemberLoadInfoDto dto = new TeamMemberLoadInfoDto(
                            teamMember.getUserId().getUserId(),
                            profile.getNickname(),
                            teamMember.getTeamMemberRole().toString(),
                            // 프로필사진까지 같이 로드하기
                            // TODO 프로필 null인지 유무 체크필요
                            profile.getProfileImageUrl());
                    return dto;
                })
                .collect(Collectors.toList());

        TeamInfoResponseDto teamInfoResponseDto = new TeamInfoResponseDto(
                teamPost.getPrjTitle(),
                teamPost.getNotionLink(),
                teamPost.getGithubLink(),
                teamInfoList
        );

        return teamInfoResponseDto;

    }

    // 팀정보 수정
    @Transactional
    public void teamInfoUpdate(Long teamId, TeamUpdateRequestDto teamUpdateRequestDto) {

        // TODO 더티채킹
        // FIXME null값을 의도적으로 주입했을때 없애는 방법이없는 오류 수정
        TeamPost teamPost = teamPostRepository.findById(teamId)
                .orElseThrow(() -> new IllegalIdentifierException("team is not found"));
        teamPost.TeamUpdate(teamUpdateRequestDto.getTeamName(),
                teamUpdateRequestDto.getGithubLink(),
                teamUpdateRequestDto.getNotionLink());

        teamPostRepository.save(teamPost);
        // TODO 팀장위임기능(따로함수분리하기)
        // TODO 팀원정보 선택후 changeLeader로 user정보 전달
        // TODO TeamId와 User로 TeamMember에서 유저찾기
        // TODO TeamLeader로 해당유저 역할 팀장으로 변경
        // TODO 본인id(권한집행자)를 TeamId, User로 TeamMember에서 찾아서 팀원으로변경

    }

    // 팀장위임
    // TODO 팀장정보를 currentUser로 받아오기?
    @Transactional
    public void changeTeamLeader(Long teamId, Long userId) {

        // TODO 팀장(로그인유저)의 member객체받아오기
        // TODO 팀장의 직책 -> 팀원으로 변경
        // TODO 업데이트해서 정보수정기능으로 접근 lock하기
        // team정보 불러오기
        TeamPost teamPost = teamPostRepository.findById(teamId)
                .orElseThrow(() -> new IllegalIdentifierException("팀을 찾을수없음."));

        // 팀장 찾아서 일반멤버로 교체
        TeamMember LeaderChangeToMember =
                teamMemberRepository.findTeamMemberByTeamIdAndTeamMemberRole(teamPost, TeamMember.Role.Team_Leader)
                        .orElseThrow(() -> new IllegalIdentifierException("팀장정보를 찾을수없음."));

        LeaderChangeToMember.setTeamMemberRole(TeamMember.Role.Team_Member);
        teamMemberRepository.save(LeaderChangeToMember);

        // 팀원 찾아서 팀장으로 교체
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalIdentifierException("유저를 찾을수없음."));

        TeamMember MemberChangeToLeader = teamMemberRepository.findByTeamIdAndUserId(teamPost, member)
                .orElseThrow(() -> new IllegalIdentifierException("팀멤버를 찾을수없음."));

        MemberChangeToLeader.setTeamMemberRole(TeamMember.Role.Team_Leader);
        teamMemberRepository.save(MemberChangeToLeader);

    }

    // 프로젝트종료버튼
    @Transactional
    public void projectComplete(Long teamId) {
        // TODO 데이터값 True면 못되돌리도록 error출력
        TeamPost teamPost = teamPostRepository.findById(teamId)
                .orElseThrow(() -> new IllegalIdentifierException("team is not found"));
        teamPost.projectEnd();
    }

    // 프로젝틑 종료되었는지 검증
    public boolean isCompleted(Long teamId) {
        TeamPost teamPost = teamPostRepository.findById(teamId)
                .orElseThrow(() -> new IllegalIdentifierException("team is not found"));

        return teamPost.getIsComplete();
    }

//    // 팀원평가
//    // TODO evaluator를 currentUser로 받아오기?
//    @Transactional
//    public void teamEvaluation(Long teamId, Long evaluator, Long evaluatee, TeamEvaluationRequestDto evaluationRequestDto) {
//        // TODO userId가 authentiction과 일치시 종료
//
//        // 팀정보 찾기
//        TeamPost teamPost = teamPostRepository.findById(teamId)
//                .orElseThrow(()-> new IllegalIdentifierException("팀정보를 찾을수없습니다."));
//
//        // 평가받는사람 찾기
//        Member member1 = memberRepository.findById(evaluatee)
//                .orElseThrow(()-> new IllegalIdentifierException("유저정보를 찾을수 없습니다."));
//        TeamMember teamMemberEvaluatee = teamMemberRepository.findByTeamIdAndUserId(teamPost,member1)
//                .orElseThrow(()-> new IllegalIdentifierException("평가받는팀원을 찾을수없습니다."));
//
//        // 평가하는사람 찾기
//        Member member2 = memberRepository.findById(evaluator)
//                .orElseThrow(()-> new IllegalIdentifierException("유저정보를 찾을수없습니다."));
//        TeamMember teamMemberEvaluator = teamMemberRepository.findByTeamIdAndUserId(teamPost, member2)
//                .orElseThrow(()-> new IllegalIdentifierException("평가자팀원을 찾을수없습니다."));
//
//        // 평가했는지 유무 검증
//        boolean isComplete = teamEvaluationRepository.existsByTeamIdAndEvaluatorAndEvaluatee(teamPost, member2, member1);
//        if (isComplete) {
//            throw new IllegalArgumentException("이미 평가를 했습니다.");
//        }
//
//        // TODO findbyUserID로 profile찾기
//        TeamEvaluation teamEvaluation = TeamEvaluation.builder()
//                .teamId(teamPost)
//                .evaluatee(teamMemberEvaluatee.getUserId()) // 평가받은사람
//                .evaluator(teamMemberEvaluator.getUserId()) // 평가자
//                .kindness(evaluationRequestDto.getKindness())
//                .promise(evaluationRequestDto.getPromise())
//                .frequency(evaluationRequestDto.getFrequency())
//                .participate(evaluationRequestDto.getParticipate())
//                .total((evaluationRequestDto.getKindness()+
//                        evaluationRequestDto.getFrequency()+
//                        evaluationRequestDto.getFrequency()+
//                        evaluationRequestDto.getParticipate())) // TODO 나누기4하지않기????
//                .build();
//        teamEvaluationRepository.save(teamEvaluation);
//
//
//        // 평가받는자 member1의 mannerScore변경
//        profileMannerScoreUpdate(member1, teamEvaluation.getTotal());
//        // 프로필 등급변경기능 호출
//        profileService.updateMannerTier(member1);
//    }
//
//    // 팀원평가시 -> 프로필 mannerScore 변경
//    // TODO member를 currentUserId로 변경 -> 팀원평가바꾸면 안해도됨.
//    private void profileMannerScoreUpdate(Member member, double evaluationTotal) {
//        // 유저의 평가받은 전체목록 조회
//        Profile profile = profileRepository.findByUserId(member);
//        double currentScore = profile.getMannerScore();
//
//        // 가중치 설정
//        double temp = evaluationTotal;
//        if (evaluationTotal <= 50 && evaluationTotal > 25) {
//            temp *= 0.04; // 최대 2점 상승
//        } else if (evaluationTotal>=0) {
//            temp *= 0.02; // 최대 0.5점 상승
//        } else if (evaluationTotal>-25) {
//            temp *= 0.04; // 최대 1점 하락
//        } else if (evaluationTotal>-40) {
//            temp *= 0.05; // 최대 2점 하락
//        } else if (evaluationTotal>=-50) {
//            temp *= 0.08; // 최대 4점 하락
//        }
//
//        //System.out.println("변경되는 숫자 = " + temp);
//
//        // 가중치 반영해서 더하기
//        currentScore += temp;
//        profile.mannserScoreUpdate(Math.round(currentScore));
//        // 저장
//        profileRepository.save(profile);
//
//    }
//
//    // 내가 평가한 팀원목록 조회
//    // TODO my에대한 식별을 CurrentUser로 받아오기?
//    public List<Map<String, Long>> myEvaluationList(Long teamId, Long userId) {
//        // 팀정보찾기
//        TeamPost teamPost = teamPostRepository.findById(teamId)
//                .orElseThrow(()-> new IllegalIdentifierException("팀정보를 찾을수없습니다."));
//
//        // 유저정보찾기
//        Member member = memberRepository.findById(userId)
//                .orElseThrow(()-> new IllegalIdentifierException("유저정보를 찾을수 없습니다."));
//
//        // 평가목록 불러오기
//        List<TeamEvaluation> teamEvaluationList = teamEvaluationRepository.findAllByTeamIdAndEvaluator(teamPost, member);
//
//        List<Map<String, Long>> myEvaluationList = teamEvaluationList.stream()
//                .map(teamEvaluation -> {
//                    Map<String, Long> map = new HashMap<>();
//                    map.put("userId", teamEvaluation.getEvaluatee().getUserId());
//                    return map;
//                })
//                .collect(Collectors.toList());
//
//        return myEvaluationList;
//
//    }
}
