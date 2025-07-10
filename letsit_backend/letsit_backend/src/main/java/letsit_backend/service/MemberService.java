package letsit_backend.service;

import letsit_backend.model.Member;
import letsit_backend.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);

    @Autowired
    private MemberRepository memberRepository;

    public Member getMemberById(Long id) {
        logger.debug("Fetching member by id: " + id);
        return memberRepository.findById(id).orElse(null);
    }
}
