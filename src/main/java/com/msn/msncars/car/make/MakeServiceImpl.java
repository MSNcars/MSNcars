package com.msn.msncars.car.make;

import com.msn.msncars.car.exception.MakeNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MakeServiceImpl implements MakeService{

    private final MakeRepository makeRepository;
    private final MakeMapper makeMapper;

    private final Logger logger = LoggerFactory.getLogger(MakeServiceImpl.class);

    public MakeServiceImpl(MakeRepository makeRepository, MakeMapper makeMapper) {
        this.makeRepository = makeRepository;
        this.makeMapper = makeMapper;
    }

    @Override
    public List<MakeSlimDTO> getAllMakes() {
        logger.debug("Entering getAllMakes method.");

        List<MakeSlimDTO> makeSlimDTOS = makeRepository.findAll().stream().map(makeMapper::toSlimDTO).toList();

        logger.debug("Makes ({}) fetched from database and mapped to dtos.", makeSlimDTOS.size());

        return makeSlimDTOS;
    }

    @Override
    public List<MakeDTO> getAllMakesWithAssociatedModels() {
        logger.debug("Entering getAllMakesWithAssociatedModels method.");

        List<MakeDTO> makeDTOS = makeRepository.findAll().stream().map(makeMapper::toDTO).toList();

        logger.debug("Makes ({}) with associated models fetched from database.", makeDTOS.size());

        return makeDTOS;
    }
    @Override
    public MakeDTO getMakeInformation(String makeName) {
        logger.debug("Entering getMakeInformation method for make: {}.", makeName);

        MakeDTO makeDTO = makeRepository.findByName(makeName).map(makeMapper::toDTO)
                .orElseThrow(() -> new MakeNotFoundException(String.format("Make with name %s was not found", makeName)));

        logger.debug("Make successfully fetched from database and mapped.");

        return makeDTO;
    }
}
