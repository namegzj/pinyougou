//定义处理器
app.service("brandService", function ($http) {

    this.findAll = function () {
        //发送请求到后台获取数据
        return $http.get("../brand/findAll.do");
    };
    //根据分页信息查询数据
    this.findPage = function (pageNo, rows) {
        return $http.get("../brand/findPage.do?pageNo=" + pageNo + "&rows=" + rows);
    };

    this.add = function(entity){
        return $http.post("../brand/add.do", entity);
    };

    this.update = function(entity){
        return $http.post("../brand/update.do", entity);
    };

    //根据id查询
    this.findOne = function (id) {
        //根据id到后台查询数据
        return $http.get("../brand/findOne.do?id=" + id);
    };

    //删除
    this.delete = function (selectedIds) {
        return $http.get("../brand/delete.do?ids=" + selectedIds);

    };

    //条件分页查询
    this.search = function (pageNo, rows, searchEntity) {
        return $http.post("../brand/search.do?pageNo=" + pageNo +"&rows=" + rows, searchEntity);

    };

    //查询品牌列表
    this.selectOptionList = function () {
        return $http.get("../brand/selectOptionList.do");

    };
})