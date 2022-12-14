package com.greedy.onoff.acc.service;


import javax.transaction.Transactional;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.greedy.onoff.acc.dto.AccDto;
import com.greedy.onoff.acc.entity.Acc;
import com.greedy.onoff.acc.repository.AccRepository;
import com.greedy.onoff.member.dto.MemberDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccService {
	
	private final AccRepository accRepository;
	private final ModelMapper modelMapper;
	
	public AccService(AccRepository accRepository,
			ModelMapper modelMapper) {
		this.accRepository = accRepository;
		this.modelMapper = modelMapper;
	}
	
	/* 수납 내역 조회 */
	public Page<AccDto> selectAccListByAccStatus(int page, String accStatus) {
		
		log.info("[AccService] selectAccListByAccStatus Start ====================");
		
		Pageable pageable = PageRequest.of(page - 1, 7, Sort.by("accCode").descending());
		
		Page<Acc> accList = accRepository.findByAccStatusContains(pageable, accStatus);
		Page<AccDto> accDtoList = accList.map(acc -> modelMapper.map(acc, AccDto.class));
		
		log.info("[AccService] AccDtoList : {}", accDtoList.getContent());
		
		log.info("[AccService] selectAccListByAccStatus End ====================");
		
		return accDtoList;
	}
	

	/* 수납 내역 상세 조회 */
	public Object selectAccForAdmin(Long accCode) {

		log.info("[AccService] selectAccForAdmin Start ====================");
		log.info("[AccService] accCode " + accCode);
		
		Acc acc = accRepository.findById(accCode)
				.orElseThrow(() -> new IllegalArgumentException("해당 내역이 없습니다!. accCode=" + accCode));
		AccDto accDto = modelMapper.map(acc, AccDto.class);
		
		
		log.info("[AccService] accDto : " + accDto);
		log.info("[AccService] selectAccForAdmin End ====================");
		
		return accDto;
	}

	/* 수납 내역 수정 */
	@Transactional
	public Object updateAcc(AccDto accDto) {
		
		Acc oriAcc = accRepository.findById(accDto.getAccCode()).orElseThrow(
				() -> new IllegalArgumentException("해당 수납 내역이 없습니다. accCode=" + accDto.getAccCode()));
		
		oriAcc.update(
				accDto.getAccDate(),
				accDto.getAccOption(),
				accDto.getAccStatus(),
				accDto.getAccContent());
				//modelMapper.map(accDto.getClassesHistory(), ClassesHistory.class));
		
		accRepository.save(oriAcc);
		
		return accDto;
	}

	

}
