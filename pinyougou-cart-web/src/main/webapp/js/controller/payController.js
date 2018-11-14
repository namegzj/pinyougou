app.controller("payController",function ($scope,cartService,$location,payService) {

    $scope.getUsername = function () {
        cartService.getUsername().success(function (response) {
            $scope.username = response.username;
        })
    };

    $scope.createNative = function () {

        //获取支付日志id
        $scope.outTradeNo = $location.search()["outTradeNo"];

        payService.createNative($scope.outTradeNo).success(function (response) {
            if ("SUCCESS" == response.result_code) {
                $scope.totalFee = (response.total_fee / 100).toFixed(2);
                //创建二维码图片
                var qr = new QRious({
                    element: document.getElementById("qrious"),
                    level:"H",
                    size:250,
                    value:response.code_url
                });
                queryPayStatus($scope.outTradeNo);
            }else {
                alert("二维码生成失败");
            }
        })
    };

    queryPayStatus = function (outTradeNO) {
        payService.queryPayStatus(outTradeNO).success(function (response) {
            if (response.success) {
                //跳转到成功页面
                location.href = "paysuccess.html#?money=" + $scope.totalFee;
            }else {
                if (response.message == "二维码超时") {
                    alert(message.response);
                    //重新生成二维码
                    $scope.createNative();
                }else {
                    //跳转到失败页面
                    location.href = "payfail.html";
                }

            }
        })
    }

    //支付成功后加载显示支付金额
    $scope.getMoney = function () {
        $scope.money = $location.search()["money"];
    };

});