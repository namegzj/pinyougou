app.controller("searchController", function ($scope,$location, searchService) {

    $scope.search = function () {
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;

            //构建分页导航条
            buildPageInfo();
        });

    };

    //定义查询和过滤条件
    $scope.searchMap = {"keywords":"", "brand":"", "category":"","spec":{}, "price":"", "pageNo":1, "pageSize":20, "sortField":"", "sort":""};

    //添加过滤条件
    $scope.addSearchItem = function (key, value) {
        if("brand" == key || "category" == key || "price" == key){
            $scope.searchMap[key] = value;
        } else {
            //规格的选项
            $scope.searchMap.spec[key] = value;
        }

        //改变查询或者过滤条件之后；页号需要从1开始
        $scope.searchMap.pageNo = 1;

        //重新查询
        $scope.search();
    };

    //删除过滤条件值
    $scope.removeSearchItem = function (key) {
        if("brand" == key || "category" == key || "price" == key){
            $scope.searchMap[key] = "";
        } else {
            //规格的选项
            delete $scope.searchMap.spec[key];
        }
        //改变查询或者过滤条件之后；页号需要从1开始
        $scope.searchMap.pageNo = 1;

        //重新查询
        $scope.search();
    };

    //构建分页导航条
    buildPageInfo = function () {
        //要在页面中显示的页号
        $scope.pageNoList = [];

        //开始显示的页号
        var startPage = 1;
        //结束显示的页号
        var endPage = $scope.resultMap.totalPages;

        //总显示的页数
        var showTotalPages = 5;

        //如果总页数大于要显示的总显示数
        if($scope.resultMap.totalPages > showTotalPages){

            //当前页的左右两边间隔页数
            var interval = Math.floor(showTotalPages/2);

            //当前页-间隔数
            startPage = parseInt($scope.searchMap.pageNo) - interval;

            //当前页+间隔数
            endPage = parseInt($scope.searchMap.pageNo) + interval;

            //如果起始页号不可以小1
            if(startPage >= 1){
                //结束页号不能大于总页数
                if(endPage > $scope.resultMap.totalPages){
                    startPage = startPage - (endPage - $scope.resultMap.totalPages);
                    endPage = $scope.resultMap.totalPages;
                }
            } else {
                //起始页号小于1；
                endPage = showTotalPages;
                startPage = 1;
            }
        }

        //前面三个点
        $scope.frontDot = false;
        if(startPage > 1){
            $scope.frontDot = true;
        }

        //后面三个点
        $scope.backDot = false;
        if(endPage < $scope.resultMap.totalPages){
            $scope.backDot = true;
        }

        for (var i = startPage; i <= endPage; i++) {
            $scope.pageNoList.push(i);
        }
    };

    $scope.isCurrentPage = function (pageNo) {
        return $scope.searchMap.pageNo == pageNo;
    };

    //根据分页页号查询
    $scope.queryByPageNo = function (pageNo) {
        pageNo = parseInt(pageNo);
        if(0 < pageNo && pageNo <= $scope.resultMap.totalPages){

            //设置查询的页号
            $scope.searchMap.pageNo = pageNo;

            $scope.search();
        }

    };

    //添加排序域和序列
    $scope.addSortField = function (field, sort) {
        $scope.searchMap.sortField = field;
        $scope.searchMap.sort = sort;

        $scope.search();
    };

    //加载浏览器地址栏中的搜索关键字
    $scope.loadKeywords = function () {
        //获取浏览器地址栏中的搜索关键字
        var keywords = $location.search()["keywords"];
        $scope.searchMap.keywords = keywords;

        $scope.search();

    };
});