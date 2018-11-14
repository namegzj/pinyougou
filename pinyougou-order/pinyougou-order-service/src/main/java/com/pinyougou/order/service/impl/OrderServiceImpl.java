package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.OrderItemMapper;
import com.pinyougou.mapper.OrderMapper;
import com.pinyougou.mapper.PayLogMapper;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl extends BaseServiceImpl<TbOrder> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private PayLogMapper payLogMapper;


    @Autowired
    private IdWorker idWorker;
    private static final String CART_LIST = "CART_LIST";

    @Override
    public PageResult search(Integer page, Integer rows, TbOrder order) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(order.get***())){
            criteria.andLike("***", "%" + order.get***() + "%");
        }*/

        List<TbOrder> list = orderMapper.selectByExample(example);
        PageInfo<TbOrder> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    /**
     * 根据购物车列表生成一个个的订单、明细、支付日志（微信支付）
     *
     * @param order 订单基本信息
     * @return 支付日志id（如果是微信支付则返回支付日志id，如果是货到付款则返回空字符串）
     */
    @Override
    public String addOrder(TbOrder order) {
        String outTradeNo = "";
        String orderIds = "";
        //1、查询redis中的购物车列表
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(CART_LIST).get(order.getUserId());
        if (cartList != null && cartList.size() > 0) {
            TbOrder tbOrder = new TbOrder();
            double orderPayment = 0.0;
            double totalPayment = 0.0;
            for (Cart cart : cartList) {
                //2、遍历每一个购物车对应生成一个订单
                tbOrder.setOrderId(idWorker.nextId());

                tbOrder.setSourceType(order.getSourceType());
                tbOrder.setUserId(String.valueOf(idWorker.nextId()));
                tbOrder.setStatus("1");

                tbOrder.setPaymentType(order.getPaymentType());
                tbOrder.setSellerId(cart.getSellerId());

                tbOrder.setCreateTime(new Date());
                tbOrder.setUpdateTime(tbOrder.getUpdateTime());

                tbOrder.setReceiver(order.getReceiver());
                tbOrder.setReceiverAreaName(order.getReceiverAreaName());
                tbOrder.setReceiverMobile(order.getReceiverMobile());


                //遍历订单明细
                for (TbOrderItem orderItem : cart.getOrderItemList()) {
                    orderItem.setId(idWorker.nextId());
                    orderItem.setOrderId(tbOrder.getOrderId());

                    orderItemMapper.insertSelective(orderItem);


                    orderPayment += orderItem.getTotalFee().doubleValue();

                }
                tbOrder.setPayment(BigDecimal.valueOf(orderPayment));

                orderMapper.insertSelective(tbOrder);

                totalPayment += orderPayment;


                if (orderIds.length() > 0) {
                    orderIds += "," + tbOrder.getOrderId();
                } else {
                    orderIds = tbOrder.getOrderId() + "";
                }

            }
            //3、如果是微信支付则需要生成支付日志
            if ("1".equals(order.getPaymentType())) {
                TbPayLog payLog = new TbPayLog();
                outTradeNo = idWorker.nextId() + "";

                payLog.setOutTradeNo(outTradeNo);
                payLog.setTradeState("0");

                payLog.setUserId(order.getUserId());
                payLog.setCreateTime(new Date());

                payLog.setTotalFee((long) (totalPayment * 100));
                payLog.setOrderList(orderIds);

                payLogMapper.insertSelective(payLog);
            }
            //4、删除redis中的购物车列表
            redisTemplate.boundHashOps(CART_LIST).delete(order.getUserId());

        }
        //5、如果是微信支付则返回支付日志id，如果是货到付款则返回空字符串
        return outTradeNo;
    }

    /**
     * 支付日志
     * @param outTradeNo
     * @return
     */
    @Override
    public TbPayLog findPayLogByOutTradeNo(String outTradeNo) {
        return payLogMapper.selectByPrimaryKey(outTradeNo);
    }

    /**
     * 更新商品状态
     * @param outTradeNo
     * @param transaction_id
     */
    @Override
    public void updateOrderStatus(String outTradeNo, String transaction_id) {
        TbPayLog paylog = findPayLogByOutTradeNo(outTradeNo);

        paylog.setTradeState("1");
        paylog.setPayTime(new Date());
        paylog.setTransactionId(transaction_id);

        payLogMapper.updateByPrimaryKeySelective(paylog);

        //更新该支付日志对应的所有订单的支付状态为已支付；2
        String[] orderIds = paylog.getOrderList().split(",");

        TbOrder order = new TbOrder();

        order.setStatus("2");
        order.setPaymentTime(new Date());

        Example example = new Example(TbOrder.class);
        example.createCriteria().andIn("orderId", Arrays.asList(orderIds));
        orderMapper.updateByExampleSelective(order, example);
    }

}
