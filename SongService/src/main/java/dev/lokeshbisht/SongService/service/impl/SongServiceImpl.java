package dev.lokeshbisht.SongService.service.impl;

import dev.lokeshbisht.SongService.dto.ApiResponseDto;
import dev.lokeshbisht.SongService.dto.MetaDataDto;
import dev.lokeshbisht.SongService.dto.song.request.SongPlayCountRequestDto;
import dev.lokeshbisht.SongService.dto.song.request.SongRequestDto;
import dev.lokeshbisht.SongService.dto.song.response.SongDto;
import dev.lokeshbisht.SongService.entity.Song;
import dev.lokeshbisht.SongService.exceptions.SongNotFoundException;
import dev.lokeshbisht.SongService.mapper.SongMapper;
import dev.lokeshbisht.SongService.repository.SongRepository;
import dev.lokeshbisht.SongService.service.SongService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class SongServiceImpl implements SongService {

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongMapper songMapper;

    private final static Logger logger = LoggerFactory.getLogger(SongServiceImpl.class);

    @Override
    public SongDto addSong(SongRequestDto songRequestDto, Long genreId) {
        logger.info("Add a new song: {} in {} genre", songRequestDto, genreId);
        Song song = songMapper.toSong(songRequestDto);
        song.setGenreId(genreId);
        song.setCreatedAt(new Date());
        return songMapper.toSongDto(songRepository.save(song));
    }

    @Override
    public SongDto updateSong(SongRequestDto songRequestDto, Long genreId, Long songId) {
        logger.info("Update song with song_id {}, and genre_id {}, with new song info {}", songId, genreId, songRequestDto);
        Optional<Song> songOldInfo = songRepository.findById(songId);
        if (songOldInfo.isEmpty()) {
            throw new SongNotFoundException("The song with songId " + songId + " doesn't exist.");
        }
        Song song = songMapper.toSong(songRequestDto);
        song.setGenreId(genreId);
        song.setId(songId);
        song.setCreatedAt(songOldInfo.get().getCreatedAt());
        song.setCreatedBy(songOldInfo.get().getCreatedBy());
        song.setUpdatedAt(new Date());
        return songMapper.toSongDto(songRepository.save(song));
    }

    @Override
    public SongDto updatePlayCount(SongPlayCountRequestDto songPlayCountRequestDto, Long songId) {
        logger.info("Update play count of song: {} to {}", songId, songPlayCountRequestDto.getPlayCount());
        Optional<Song> song = songRepository.findById(songId);
        if (song.isEmpty()) {
            throw new SongNotFoundException("The song with songId " + songId + " doesn't exist.");
        }
        song.get().setStreams(songPlayCountRequestDto.getPlayCount());
        return songMapper.toSongDto(songRepository.save(song.get()));
    }

    @Override
    public void deleteSong(Long songId) {
        logger.info("Delete song: {}", songId);
        Optional<Song> song = songRepository.findById(songId);
        if (song.isEmpty()) {
            throw new SongNotFoundException("The song with songId " + songId + " doesn't exist.");
        }
        songRepository.delete(song.get());
    }

    private MetaDataDto generateApiResponseMetadata(int page, int size, int total, double startTime) {
        return MetaDataDto.builder()
            .page(page)
            .size(size)
            .total(total)
            .took(System.currentTimeMillis() - startTime)
            .build();
    }

    @Override
    public ApiResponseDto<SongDto> getSong(Long songId) {
        logger.info("Fetch song: {}", songId);
        double startTime = System.currentTimeMillis();
        Optional<Song> song = songRepository.findById(songId);
        if (song.isEmpty()) {
            throw new SongNotFoundException("The song with songId " + songId + " doesn't exist.");
        }
        SongDto songDto = songMapper.toSongDto(song.get());
        return new ApiResponseDto<>(songDto, "OK", null, generateApiResponseMetadata(1, 1, 1, startTime));
    }
}
