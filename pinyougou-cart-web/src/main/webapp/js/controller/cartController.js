app.controller("cartController",function ($scope,cartService) {

    //显示用户名
    $scope.getUsername = function () {
        cartService.getUsername().success(function (response) {
            $scope.username = response.username;
        })
    };

    //查询购物车
    $scope.findCartList = function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList = response;
            $scope.totalValue = cartService.sumTotalValue(response);

        })
    };

    $scope.addItemToCartList = function (itemId, num) {
        cartService.addItemToCartList(itemId, num).success(function (response) {
            if (response.success) {
                $scope.findCartList();
            }else {
                alert(response.message);
            }
        })

    }
});