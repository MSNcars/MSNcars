package com.msn.msncars.car.make;

import com.msn.msncars.car.exception.MakeNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MakeServiceImpl implements MakeService{

    private final MakeRepository makeRepository;
    private final MakeMapper makeMapper;

    public MakeServiceImpl(MakeRepository makeRepository, MakeMapper makeMapper) {
        this.makeRepository = makeRepository;
        this.makeMapper = makeMapper;
    }

    @Override
    public List<MakeSlimDTO> getAllMakes() {
        return makeRepository.findAll().stream().map(makeMapper::toSlimDTO).toList();
    }

    @Override
    public List<MakeDTO> getAllMakesWithAssociatedModels() {
        return makeRepository.findAll().stream().map(makeMapper::toDTO).toList();
    }
    @Override
    public MakeDTO getMakeInformation(String makeName) {
        return makeRepository.findByName(makeName).map(makeMapper::toDTO)
                .orElseThrow(() -> new MakeNotFoundException(String.format("Make with name %s was not found", makeName)));
    }
}
