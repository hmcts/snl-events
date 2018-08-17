package uk.gov.hmcts.reform.sandl.snlevents.testdata.helpers;

import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

public class ReferenceDataValidator<M, R, I1 extends Serializable, I2 extends Serializable>  {
    M savedMainType;
    R savedReferencedObj;
    JpaRepository<M, I1> mainDataRepository;

    public ReferenceDataValidator<M, R, I1, I2> save(JpaRepository<M, I1> mainDataRepository, M modifiedObj) {
        this.mainDataRepository = mainDataRepository;
        mainDataRepository.saveAndFlush(modifiedObj);
        return this;
    }

    public ReferenceDataValidator<M, R, I1, I2> fetchAgain(
        I1 modifiedObjId,
        I2 referencedObjId,
        JpaRepository<R, I2> referencedDataRepository) {

        this.savedMainType = mainDataRepository.findOne(modifiedObjId);
        this.savedReferencedObj = referencedDataRepository.findOne(referencedObjId);
        return this;
    }

    public ReferenceDataValidator<M, R, I1, I2> verifyThatRelationsBetweenObjAreSet(
        Function<M, Set<R>> getReferencedObjs,
        Function<R, Set<M>> getInvertedReferencedObj) {

        assertThat(getReferencedObjs.apply(savedMainType).size()).isEqualTo(1);
        assertThat(getInvertedReferencedObj.apply(savedReferencedObj).size()).isEqualTo(1);
        return this;
    }
}
