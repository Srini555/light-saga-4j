package com.networknt.saga.repository;


import com.networknt.eventuate.common.impl.JSonMapper;
import com.networknt.saga.core.command.common.CommandMessageHeaders;
import com.networknt.saga.core.message.common.Message;
import com.networknt.saga.core.message.producer.MessageBuilder;
import com.networknt.saga.core.message.producer.MessageProducer;
import com.networknt.saga.orchestration.DestinationAndResource;
import com.networknt.saga.orchestration.SagaInstance;
import com.networknt.saga.orchestration.SerializedSagaData;
import com.networknt.service.SingletonServiceFactory;
import org.h2.tools.RunScript;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertNotNull;


/**
 * Junit test class for SagaInstanceRepositoryJdbc.
 * use H2 test database for data source
 */
public class SagaInstanceRepositoryTest {

    public static DataSource ds;

    static {
        ds = (DataSource) SingletonServiceFactory.getBean(DataSource.class);
       try (Connection connection = ds.getConnection()) {
            // Runscript doesn't work need to execute batch here.
            String schemaResourceName = "/queryside_ddl.sql";
            InputStream in = SagaInstanceRepositoryTest.class.getResourceAsStream(schemaResourceName);

            if (in == null) {
                throw new RuntimeException("Failed to load resource: " + schemaResourceName);
            }
            InputStreamReader reader = new InputStreamReader(in);
            RunScript.execute(connection, reader);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private SagaInstanceRepository  sagaInstanceRepository = (SagaInstanceRepository) SingletonServiceFactory.getBean(SagaInstanceRepository.class);


    private static SagaInstance sagaInstance;


    @BeforeClass
    public static void setUp() {
        Set<DestinationAndResource> destinationsAndResources = new HashSet<>();
        DestinationAndResource destinationAndResource = new DestinationAndResource("orderService", "source");
        destinationsAndResources.add(destinationAndResource);
        SerializedSagaData SerializedSagaData = new SerializedSagaData("orderTest", "<sagaData>data</sagaData>");
        sagaInstance = new SagaInstance("order", null, "sagaOrder", "user", SerializedSagaData, destinationsAndResources);
    }

    @Test
    public void testSave() {
        sagaInstanceRepository.save(sagaInstance);
        System.out.println("sagaId" + sagaInstance.getId());
        System.out.println("sagaType" + sagaInstance.getSagaType());
        SagaInstance ins = sagaInstanceRepository.find(sagaInstance.getSagaType(), sagaInstance.getId());
        assertNotNull(ins);
    }

    @Test
    public void testUpdate() {
        sagaInstanceRepository.save(sagaInstance);
        System.out.println("sagaId" + sagaInstance.getId());
        System.out.println("sagaType" + sagaInstance.getSagaType());
        SagaInstance ins = sagaInstanceRepository.find(sagaInstance.getSagaType(), sagaInstance.getId());
        assertNotNull(ins);
        sagaInstance.setStateName("new_state");
        sagaInstanceRepository.update(sagaInstance);
    }

}
