package com.networknt.saga.orchestration;

import com.networknt.saga.orchestration.AggregateInstanceSubscriptionsDAO;
import com.networknt.saga.orchestration.EventClassAndAggregateId;
import com.networknt.saga.orchestration.SagaTypeAndId;
import com.networknt.service.SingletonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class AggregateInstanceSubscriptionsDAOImpl implements AggregateInstanceSubscriptionsDAO {

    private Logger logger = LoggerFactory.getLogger(getClass());
    private DataSource dataSource = SingletonServiceFactory.getBean(DataSource.class);

    public AggregateInstanceSubscriptionsDAOImpl() {
    }

    @Override
    public void update(String sagaType, String sagaId, List<EventClassAndAggregateId> eventHandlers) {

        String psDelete = "DELETE FROM aggre_instance_subscriptions WHERE saga_type = ? AND saga_id =?";
        String psInsert = "INSERT INTO aggre_instance_subscriptions(aggregate_id, event_type, saga_type, saga_id) values(?, ?, ?, ?)";
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            PreparedStatement stmt = connection.prepareStatement(psDelete);
            stmt.setString(1, sagaType);
            stmt.setString(2, sagaId);
            stmt.executeUpdate();
            PreparedStatement ps = connection.prepareStatement(psInsert);
            for  (EventClassAndAggregateId eventClassAndAggregateId : eventHandlers) {
                ps.setString(1, Long.toString(eventClassAndAggregateId.getAggregateId()));
                ps.setString(2, eventClassAndAggregateId.getEventClass().getName());
                ps.setString(3, sagaType);
                ps.setString(4, sagaId);
                ps.addBatch();
            }
            ps.executeBatch();
            connection.commit();
        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }

    }

    @Override
    public List<SagaTypeAndId> findSagas(String aggregateType, String aggregateId, String eventType){
        String psSelect = "Select saga_type, saga_id from aggre_instance_subscriptions where aggregate_id = ? and event_type = ?";

        List<SagaTypeAndId> sagas = new ArrayList<>();
        try (final Connection connection = dataSource.getConnection()) {

            PreparedStatement ps = connection.prepareStatement(psSelect);
            ps.setString(1, aggregateId);
            ps.setString(2, eventType);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sagas.add(new SagaTypeAndId(rs.getString("saga_type"), rs.getString("saga_id") ));
            }
        } catch (SQLException e) {
            logger.error("SqlException:", e);
        }
        return sagas;
    }

}
