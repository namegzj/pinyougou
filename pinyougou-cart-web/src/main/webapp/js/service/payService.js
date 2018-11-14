app.service("payService",function ($http) {

    this.createNative = function (outTradeNo) {
        return $http.get("pay/createNative.do?outTradeNo=" + outTradeNo);
    };

    this.queryPayStatus = function (outTraderNo) {
        return $http.get("pay/queryPayStatus.do?outTradeNo=" + outTraderNo);
    }
});