//定义处理器
app.controller("brandController", function ($scope, $http, $controller, brandService) {
    // 查询所有列表数据并绑定到 list 对象
    $scope.findAll = function () {
        brandService.findAll().success(function (response) {
            $scope.list = response;
        });
    };
// 初始化分页参数
    $scope.paginationConf = {
        currentPage: 1,// 当前页号
        totalItems: 10,// 总记录数
        itemsPerPage: 10,// 页大小
        perPageOptions: [10, 20, 30, 40, 50],// 可选择的每页大小
        onChange: function () {// 当上述的参数发生变化了后触发
            $scope.reloadList();
        }
    };
    // 加载表格数据
    $scope.reloadList = function () {
    $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage)
};

    //根据分页信息查询数据
    $scope.findPage = function (pageNo, rows) {
        brandService.findPage(pageNo, rows).success(function (response) {
            //response是分页对象（列表，总记录数）
            $scope.list = response.rows;
            //给分页组件重新设置最新的总记录数
            $scope.paginationConf.totalItems = response.total;
        });

    };

    //保存
    $scope.save = function () {
        var obj;
        if($scope.entity.id != null){
            //修改
            obj = brandService.update($scope.entity);
        } else {
            obj = brandService.add($scope.entity);
        }

        obj.success(function (response) {
            if(response.success){
                //如果操作成功；则刷新列表
                $scope.reloadList();
            } else {
                alert(response.message);
            }

        });

    };

    //数据回显
    $scope.findOne = function (id) {
        //根据id到后台查询数据
        brandService.findOne(id).success(function (response) {
            $scope.entity = response;
        });
    };

    // 定义一个放置选择了 id 的数组
    $scope.selectedIds = [];
    $scope.updateSelection = function ($event, id) {
        if ($event.target.checked) {
            $scope.selectedIds.push(id);
        } else {
            var index = $scope.selectedIds.indexOf(id);
            $scope.selectedIds.splice(index, 1);
        }
    };

    //删除
    $scope.delete = function () {
        if($scope.selectedIds.length == 0){
            alert("请先选择要删除的记录");
            return;
        }
        //confirm会弹出确认窗口；如果点击 确定则返回true,否则false
        if(confirm("你确定要删除选中了的那些记录吗？")){
            brandService.delete($scope.selectedIds).success(function (response) {
                if(response.success){
                    //刷新列表
                    $scope.reloadList();
                    //重置数组
                    $scope.selectedIds = [];
                } else {
                    alert(response.message);
                }

            });
        }

    };

    // 定一个空的搜索条件对象
    $scope.searchEntity = {};

    //条件分页查询
    $scope.search = function (pageNo, rows) {
        brandService.search(pageNo, rows, $scope.searchEntity).success(function (response) {
            $scope.list = response.rows;

            //更新分页组件的总记录数
            $scope.paginationConf.totalItems = response.total;
        });

    };
})