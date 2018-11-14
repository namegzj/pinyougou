app.controller("orderInfoController",function ($scope,addressService,cartService) {

    $scope.getUsername = function () {
        cartService.getUsername().success(function (response) {
            $scope.username = response.username;
        })
    };

    $scope.findAddressList = function () {
        addressService.findAddressList().success(function (response) {

            $scope.addressList = response;

            for (var i = 0; i < response.length; i++) {

                var address = response[i];
                if ("1"== address.isDefault) {
                    $scope.address = address;
                    break;
                }

            }
        })
    };

    $scope.isSelectedAddress = function (address) {
      return  $scope.address == address;

    };

    $scope.selectedAddress = function (address) {
        $scope.address = address;
    };

    $scope.order = {paymentType:"1"};

    $scope.selectPaymentType = function (type) {
        $scope.order.paymentType = type;
    };

    //查询购物车列表
    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;

            //计算总数量和总价
            $scope.totalValue = cartService.sumTotalValue(response);
        });

    };

    $scope.submitOrder = function () {

        //设置订单收件人信息
        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiver = $scope.address.contact;

        cartService.submitOrder($scope.order).success(function (response) {
            if (response.success){
                if ($scope.order.paymentType == "1"){
                    location.href = "pay.html#?outTradeNo=" + response.message;
                }else {
                    location.href = "paysuccess.html"
                }

            }else {
                alert(response.message);
            }

        })
    }
});