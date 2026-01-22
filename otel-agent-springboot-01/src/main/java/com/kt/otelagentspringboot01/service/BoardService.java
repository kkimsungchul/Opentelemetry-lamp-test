package com.kt.otelagentspringboot01.service;



import com.kt.otelagentspringboot01.domain.Board;
import com.kt.otelagentspringboot01.domain.LogApi;
import com.kt.otelagentspringboot01.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final LogService logService;

    public List<Board> all(){
        LogApi logApi = logService.save();
        List<Board> boardList = boardRepository.findAll();
        logService.update(logApi);
        return boardList;
    }


    //@WithSpan
    public List<Board> all(int pageSize){
        LogApi logApi = logService.save();
        PageRequest pageRequest = PageRequest.of(0, pageSize);
        List<Board> boardList = boardRepository.findAll(pageRequest).getContent();
        logService.update(logApi);

        return boardList;
    }
}
