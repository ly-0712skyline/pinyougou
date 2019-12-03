var app = new Vue({
    el: "#app",
    data: {
        //数据列表
        entityList: [],
        //总记录数
        total: 0,
        //页大小
        pageSize: 10,
        //当前页号
        pageNum: 1,
        //数据
        entity: {},
        //要删除的id数组
        ids: [],
        //搜索条件
        searchEntity:{},
        //是否选中
        selectedFlag: false

    },
    methods: {
        //全选
        selectAll: function(){
          if(!this.selectedFlag){
              //选中
              for (let i = 0; i < this.entityList.length; i++) {
                  const entity = this.entityList[i];
                  this.ids[i] = entity.id;
              }
          } else {
              this.ids = [];
          }
        },
        //删除
        deleteList: function(){
            if (this.ids.length == 0) {
                alert("请先选择之后再删除！");
                return;
            }
            //confirm 如果点击 确认则true，否则返回false
            if (confirm("真的要删除选择了的那些记录吗？")) {
                axios.get("../brand/delete.do?ids=" + this.ids).then(function (respose) {
                    if(respose.data.success){
                        app.ids = [];
                        app.searchList(1);
                    } else {
                        alert(respose.data.message);
                    }
                });
            }
        },
        //根据id查询
        findOne: function (id) {
            axios.get("../brand/findOne/" + id + ".do").then(function (response) {
                app.entity = response.data;
            });
        },
        //保存数据
        save: function () {
            var method = "add";
            if (this.entity.id != null) {
                method = "update";
            }

            axios.post("../brand/" + method + ".do", this.entity).then(function (response) {
                if (response.data.success) {
                    //刷新列表
                    app.searchList(1);
                } else {
                    alert(response.data.message);
                }
            });
        },
        //查询方法
        searchList: function (curPage) {
            this.pageNum = curPage;

            /*axios.get("../brand/findPage.do?pageNum=" + this.pageNum + "&pageSize=" + this.pageSize).then(function (response) {
                //设置总记录数
                app.total = response.data.total;
                //设置列表
                app.entityList = response.data.list;
            });*/
            axios.post("../brand/search.do?pageNum=" + this.pageNum + "&pageSize="+this.pageSize, this.searchEntity).then(function (response) {
                //设置总记录数
                app.total = response.data.total;
                //设置列表
                app.entityList = response.data.list;
            });

        }
    },
    //监听数据属性的变化
    watch :{
        ids: function (newValue, oldValue) {
            if (this.ids.length != this.entityList.length) {
                this.selectedFlag = false;
            } else{
                this.selectedFlag = true;
            }
        }
    },
    created() {
        //加载品牌数据
        /*
                    axios.get("../brand/findAll.do").then(function (response) {
                        console.log(response);
                        //因为在axios的回调方法中this表示窗口window，不是vue对象；app 是vue对象对应的变量名称
                        app.entityList = response.data;
                    });
        */
        this.searchList(1);
    }
});
