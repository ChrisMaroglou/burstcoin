package brs.http;

import static brs.http.JSONResponses.INCORRECT_DELIVERY_DEADLINE_TIMESTAMP;
import static brs.http.JSONResponses.INCORRECT_PURCHASE_PRICE;
import static brs.http.JSONResponses.INCORRECT_PURCHASE_QUANTITY;
import static brs.http.JSONResponses.MISSING_DELIVERY_DEADLINE_TIMESTAMP;
import static brs.http.JSONResponses.UNKNOWN_GOODS;
import static brs.http.common.Parameters.DELIVERY_DEADLINE_TIMESTAMP_PARAMETER;
import static brs.http.common.Parameters.PRICE_NQT_PARAMETER;
import static brs.http.common.Parameters.QUANTITY_PARAMETER;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import brs.Account;
import brs.Blockchain;
import brs.BurstException;
import brs.DigitalGoodsStore.Goods;
import brs.TransactionProcessor;
import brs.common.QuickMocker;
import brs.common.QuickMocker.MockParam;
import brs.services.AccountService;
import brs.services.ParameterService;
import brs.services.TimeService;
import brs.services.TransactionService;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;

public class DGSPurchaseTest extends AbstractTransactionTest {

  private DGSPurchase t;

  private ParameterService mockParameterService;
  private Blockchain mockBlockchain;
  private AccountService mockAccountService;
  private TransactionProcessor mockTransactionProcessor;
  private TimeService mockTimeService;
  private TransactionService transactionServiceMock;

  @Before
  public void setUp() {
    mockParameterService = mock(ParameterService.class);
    mockBlockchain = mock(Blockchain.class);
    mockAccountService = mock(AccountService.class);
    mockTransactionProcessor = mock(TransactionProcessor.class);
    mockTimeService = mock(TimeService.class);
    transactionServiceMock = mock(TransactionService.class);

    t = new DGSPurchase(mockParameterService, mockTransactionProcessor, mockBlockchain, mockAccountService, mockTimeService, transactionServiceMock);
  }

  @Test
  public void processRequest() throws BurstException {
    final int goodsQuantity = 5;
    final long goodsPrice = 10L;
    final long deliveryDeadlineTimestamp = 100;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(QUANTITY_PARAMETER, goodsQuantity),
        new MockParam(PRICE_NQT_PARAMETER, goodsPrice),
        new MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, deliveryDeadlineTimestamp)
    );

    final long mockSellerId = 123L;
    final Goods mockGoods = mock(Goods.class);
    when(mockGoods.isDelisted()).thenReturn(false);
    when(mockGoods.getQuantity()).thenReturn(10);
    when(mockGoods.getPriceNQT()).thenReturn(10L);
    when(mockGoods.getSellerId()).thenReturn(mockSellerId);

    final Account mockBuyerAccount = mock(Account.class);
    final Account mockSellerAccount = mock(Account.class);

    when(mockParameterService.getGoods(eq(req))).thenReturn(mockGoods);
    when(mockTimeService.getEpochTime()).thenReturn(10);

    when(mockAccountService.getAccount(eq(mockSellerId))).thenReturn(mockSellerAccount);

    super.prepareTransactionTest(req, mockParameterService, mockTransactionProcessor, mockBuyerAccount);

    t.processRequest(req);
  }

  @Test
  public void processRequest_unknownGoods() throws BurstException {
    final HttpServletRequest req = QuickMocker.httpServletRequest();

    final Goods mockGoods = mock(Goods.class);
    when(mockGoods.isDelisted()).thenReturn(true);

    when(mockParameterService.getGoods(eq(req))).thenReturn(mockGoods);

    assertEquals(UNKNOWN_GOODS, t.processRequest(req));
  }

  @Test
  public void processRequest_incorrectPurchaseQuantity() throws BurstException {
    final int goodsQuantity = 5;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(QUANTITY_PARAMETER, goodsQuantity)
    );

    final Goods mockGoods = mock(Goods.class);
    when(mockGoods.isDelisted()).thenReturn(false);
    when(mockGoods.getQuantity()).thenReturn(4);

    when(mockParameterService.getGoods(eq(req))).thenReturn(mockGoods);

    assertEquals(INCORRECT_PURCHASE_QUANTITY, t.processRequest(req));
  }

  @Test
  public void processRequest_incorrectPurchasePrice() throws BurstException {
    final int goodsQuantity = 5;
    final long goodsPrice = 5L;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(QUANTITY_PARAMETER, goodsQuantity),
        new MockParam(PRICE_NQT_PARAMETER, goodsPrice)
    );

    final Goods mockGoods = mock(Goods.class);
    when(mockGoods.isDelisted()).thenReturn(false);
    when(mockGoods.getQuantity()).thenReturn(10);
    when(mockGoods.getPriceNQT()).thenReturn(10L);

    when(mockParameterService.getGoods(eq(req))).thenReturn(mockGoods);

    assertEquals(INCORRECT_PURCHASE_PRICE, t.processRequest(req));
  }


  @Test
  public void processRequest_missingDeliveryDeadlineTimestamp() throws BurstException {
    final int goodsQuantity = 5;
    final long goodsPrice = 10L;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(QUANTITY_PARAMETER, goodsQuantity),
        new MockParam(PRICE_NQT_PARAMETER, goodsPrice)
    );

    final Goods mockGoods = mock(Goods.class);
    when(mockGoods.isDelisted()).thenReturn(false);
    when(mockGoods.getQuantity()).thenReturn(10);
    when(mockGoods.getPriceNQT()).thenReturn(10L);

    when(mockParameterService.getGoods(eq(req))).thenReturn(mockGoods);

    assertEquals(MISSING_DELIVERY_DEADLINE_TIMESTAMP, t.processRequest(req));
  }

  @Test
  public void processRequest_incorrectDeliveryDeadlineTimestamp_unParsable() throws BurstException {
    final int goodsQuantity = 5;
    final long goodsPrice = 10L;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(QUANTITY_PARAMETER, goodsQuantity),
        new MockParam(PRICE_NQT_PARAMETER, goodsPrice),
        new MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, "unParsable")
    );

    final Goods mockGoods = mock(Goods.class);
    when(mockGoods.isDelisted()).thenReturn(false);
    when(mockGoods.getQuantity()).thenReturn(10);
    when(mockGoods.getPriceNQT()).thenReturn(10L);

    when(mockParameterService.getGoods(eq(req))).thenReturn(mockGoods);

    assertEquals(INCORRECT_DELIVERY_DEADLINE_TIMESTAMP, t.processRequest(req));
  }

  @Test
  public void processRequest_incorrectDeliveryDeadlineTimestamp_beforeCurrentTime() throws BurstException {
    final int goodsQuantity = 5;
    final long goodsPrice = 10L;
    final long deliveryDeadlineTimestamp = 100;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
        new MockParam(QUANTITY_PARAMETER, goodsQuantity),
        new MockParam(PRICE_NQT_PARAMETER, goodsPrice),
        new MockParam(DELIVERY_DEADLINE_TIMESTAMP_PARAMETER, deliveryDeadlineTimestamp)
    );

    final Goods mockGoods = mock(Goods.class);
    when(mockGoods.isDelisted()).thenReturn(false);
    when(mockGoods.getQuantity()).thenReturn(10);
    when(mockGoods.getPriceNQT()).thenReturn(10L);

    when(mockParameterService.getGoods(eq(req))).thenReturn(mockGoods);
    when(mockTimeService.getEpochTime()).thenReturn(1000);

    assertEquals(INCORRECT_DELIVERY_DEADLINE_TIMESTAMP, t.processRequest(req));
  }
}
