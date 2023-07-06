package team.themoment.hellogsm.batch.jobs.setRegistrationNumber;

import team.themoment.hellogsm.entity.domain.application.enums.Screening;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RegistrationNumberSequence {
    private Map<Screening, Integer> sequenceMap =
            new HashMap(Map.of(
                    Screening.GENERAL, 0,
                    Screening.SOCIAL, 0,
                    Screening.SPECIAL_VETERANS, 0,
                    Screening.SPECIAL_ADMISSION, 0
            ));

    public void add(Screening screening) {
        if (screening == Screening.SPECIAL_VETERANS || screening == Screening.SPECIAL_ADMISSION) {
            // SPECIAL_VETERANS와 SPECIAL_ADMISSION는 같은 시퀀스 값을 공유
            sequenceMap.put(Screening.SPECIAL_VETERANS, sequenceMap.get(Screening.SPECIAL_VETERANS) + 1);
            sequenceMap.put(Screening.SPECIAL_ADMISSION, sequenceMap.get(Screening.SPECIAL_VETERANS));
        } else {
            sequenceMap.put(screening, sequenceMap.get(screening) + 1);
        }
    }

    public Integer get(Screening screening) {
        return sequenceMap.get(screening);
    }

    public void init() {
        sequenceMap = new HashMap(Map.of(
                Screening.GENERAL, 0,
                Screening.SOCIAL, 0,
                Screening.SPECIAL_VETERANS, 0,
                Screening.SPECIAL_ADMISSION, 0
        ));
    }

    public void clear() {
        sequenceMap = Collections.emptyMap();
    }
}
