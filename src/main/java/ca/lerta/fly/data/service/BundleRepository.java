package ca.lerta.fly.data.service;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.lerta.fly.data.entity.Bundle;
import ca.lerta.fly.data.entity.FlyApplication;
import ca.lerta.fly.inmemory.InMemoryJpaRepository;
import ca.lerta.fly.inmemory.UUIDPrimaryKeyGenerator;

@Component
@Primary
public class BundleRepository extends InMemoryJpaRepository<Bundle, UUID> {

    ObjectMapper mapper = new ObjectMapper();
    private Logger logger = (Logger) LoggerFactory.getLogger(BundleRepository.class);

    private final FlyApplicationRepository flyAppRepository;

    @Autowired
    public BundleRepository(FlyApplicationRepository flyAppRepository) {
        this.flyAppRepository = flyAppRepository;
        this.setPrimaryKeyGenerator(new UUIDPrimaryKeyGenerator());
    }

    public void loadRepository(String accessToken) {
        this.deleteAll();
        flyAppRepository.loadRepository(accessToken);
        Map<String,List<FlyApplication>> appsByName = flyAppRepository.findAll().stream().collect(Collectors.groupingBy(FlyApplication::getBundle));
        SortedMap<String,List<FlyApplication>> sortedAppsByName = new TreeMap<>(appsByName);
        for(Entry<String, List<FlyApplication>> me: sortedAppsByName.entrySet()) {
            Bundle bundle = new Bundle();
            bundle.setBundleName(me.getKey());
            for (FlyApplication a: me.getValue()) {
                switch (a.getAppType()) {
                    case FlyApplication.OWLCMS:
                    bundle.setOwlcmsName(a.getName());
                    bundle.setOwlcmsActualRunning(a.isRunning());
                    break;
                    case FlyApplication.RESULTS:
                    bundle.setResultsName(a.getName());
                    bundle.setResultsActualRunning(a.isRunning());
                    break;
                    case FlyApplication.DB:
                    bundle.setDBName(a.getName());
                    bundle.setDBActualRunning(a.isRunning());
                    break;
                }
            }
            logger.warn("bundle {} running={}", bundle.getBundleName(), bundle.isOwlcmsActualRunning());
            this.save(bundle);
        }
    }
}
