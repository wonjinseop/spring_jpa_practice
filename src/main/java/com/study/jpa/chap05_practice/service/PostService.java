package com.study.jpa.chap05_practice.service;

import com.study.jpa.chap05_practice.dto.*;
import com.study.jpa.chap05_practice.entity.HashTag;
import com.study.jpa.chap05_practice.entity.Post;
import com.study.jpa.chap05_practice.repository.HashTagRepository;
import com.study.jpa.chap05_practice.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional // JPA 레파지토리는 트랜잭션 단위로 동작하기 때문에 작성해 주세요.
public class PostService {
    
    private final PostRepository postRepository;
    private final HashTagRepository hashTagRepository;
    
    public PostListResponseDTO getPosts(PageDTO dto) {
        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(
                dto.getPage() - 1,
                dto.getSize(),
                Sort.by("createDate").descending());
        
        // 데이터베이스에서 게시물 목록 조회
        Page<Post> posts = postRepository.findAll(pageable);
        
        // 게시물 정보만 꺼내기
        List<Post> postList = posts.getContent();
        
        // 게시물 정보를 응답용 DTO의 형태에 맞게 변환
        List<PostDetailResponseDTO> detailList = postList.stream()
                .map(PostDetailResponseDTO::new)
                .toList();
        
        // DB에서 조회한 정보들을 JSON 형태에 맞는 DTO로 변환.
        // 페이지 구성 정보와 위에 있는 게시물 정보를 또다른 DTO로 한번에 포장해서 리턴할 예정.
        // -> PostListResponseDTO
        
        return PostListResponseDTO.builder()
                // 총 게시물의 수가 아니라 페이징에 의해 조회된 게시물의 개수
                .count(detailList.size())
                // JPA가 준 페이지 정보가 담긴 객체를 DTO에게 전달해서 그쪽에서 알고리즘 돌리게 시킴.
                .pageInfo(new PageResponseDTO(posts))
                .posts(detailList)
                .build();
        
    }
    
    public PostDetailResponseDTO getDetail(Long id) throws Exception {
        
        Post post = getPost(id);
        
        return new PostDetailResponseDTO(post);
        
    }
    
    private Post getPost(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(id + "번 게시물이 존재하지 않습니다."));
    }
    
    public PostDetailResponseDTO insert(PostCreateDTO dto) throws Exception {
        
        // 게시물 저장 (아직 해시태그는 insert 되지 않음
        Post saved = postRepository.save(dto.toEntity());
        
        List<String> hashTags = dto.getHashTags();
        if (hashTags != null && !hashTags.isEmpty()) {
            hashTags.forEach(ht -> {
                HashTag hashTag = HashTag.builder()
                        .tagName(ht)
                        .post(saved)
                        .build();
                HashTag savedTag = hashTagRepository.save(hashTag);
                
                /*
                    Post Entity는 DB에 save를 진행할 때 HashTag에 대한 내용을 갱신하지 않습니다.
                    HashTag Entity는 따로 save를 진행합니다. (테이블이 각각 나뉘어 있음)
                    HashTag는 양방향 맵핑이 되어있는 연관관계의 주인이기 때문에 svae를 진행할 때 Post를 전달하므로
                    DB와 Entity의 상태가 동일합니다.
                    Post는 HashTag의 정보가 비어있는 상태입니다.
                    Post Entity에 연관관계 편의 메서드를 작성하여 save된 HashTag의 내용을 동기화 해야
                    추후에 진행되는 과정에서 문제가 발생하지 않습니다.
                    (Post를 화면단으로 return -> HashTag들도 같이 가야 함. -> 직접 갱신)
                    (Post를 다시 SELECT해서 가져온다? -> INSERT가 완료된 후에 SELECT를 때려야 됨 -> Entity Manager로 강제 flush()
                    INSET는 트랜잭션 종료 후 실행.)
                 */
                saved.addHashTag(savedTag);
            });
        }
        
        // 방금 insert 요청한 게시물 정보를 DTO로 변환해서 리턴.
        return new PostDetailResponseDTO(saved);
    }
    
    public PostDetailResponseDTO modify(PostModifyDTO dto) {
        
        // 수정 전 데이터를 조회
        Post postEntity = getPost(dto.getPostNo());
        
        // 수정 시작
        postEntity.setTitle(dto.getTitle());
        postEntity.setContent(dto.getContent());
        
        // 수정 완료
        Post modifiedPost = postRepository.save(postEntity);
        
        return new PostDetailResponseDTO(modifiedPost);
        
    }
    
    public void delete(Long id) {
//        Post post = getPost(id);
//        List<HashTag> tags = hashTagRepository.findAll();
//        tags.forEach(ht -> {
//            if(ht.getPost().getId() == id) {
//                hashTagRepository.delete(ht);
//            }
//        });
//        postRepository.delete(post);
        postRepository.deleteById(id);
    }
}
