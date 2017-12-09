package com.networknt.saga.order.service;


import com.networknt.saga.orchestration.SagaManager;
import com.networknt.saga.order.domain.Order;
import com.networknt.saga.order.domain.OrderDetails;
import com.networknt.saga.order.domain.OrderRepository;
import com.networknt.saga.order.saga.createorder.CreateOrderSagaData;
import com.networknt.tram.event.ResultWithEvents;

public class OrderService {


  private SagaManager<CreateOrderSagaData> createOrderSagaManager;


  private OrderRepository orderRepository;

  public OrderService(OrderRepository orderRepository, SagaManager<CreateOrderSagaData> createOrderSagaManager) {
    this.orderRepository = orderRepository;
    this.createOrderSagaManager = createOrderSagaManager;
  }

  public Order createOrder(OrderDetails orderDetails) {
    ResultWithEvents<Order> oe = Order.createOrder(orderDetails);
    Order order = oe.result;
    orderRepository.save(order);
    CreateOrderSagaData data = new CreateOrderSagaData(order.getId(), orderDetails);
    createOrderSagaManager.create(data, Order.class, order.getId());
    return order;
  }

}
