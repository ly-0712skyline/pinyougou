var app = new Vue({
    el: "#app",
    data: {
        //搜索条件
        searchMap:{"keywords":"","category":"","brand":"","spec":{}, "price":"", "pageNo":1, "pageSize":10, "sortField":"", "sortOrder":""},
        //返回结果
        resultMap:{},
        //分页页号数组
        pageNoList:[],
        //分页导航条前面3个点
        frontDot:false,
        //分页导航条后面3个点
        backDot:false
    },
    methods:{
        //排序
        sortSearch: function(sortField, sortOrder){
          this.searchMap.sortField = sortField;
          this.searchMap.sortOrder = sortOrder;

          //页号重置
          this.searchMap.pageNo = 1;

          this.search();
        },
        //根据页号查询
        queryByPageNo: function(pageNo){
            if(pageNo > 0 && pageNo <= this.resultMap.totalPages){
                this.searchMap.pageNo = pageNo;
                this.search();
            }
        },
        //删除过滤条件
        removeSearchItem: function(key){
            if ("category" == key || "brand" == key || "price" == key) {
                //参数1：要设置的对象，参数2：设置的属性，参数3：属性对应的值
                this.$set(this.searchMap, key, "");
            } else {
                //参数1：要设置的对象，参数2：设置的属性，参数3：属性对应的值
                this.$set(this.searchMap.spec, key, null);//为了页面及时显示

                delete this.searchMap.spec[key];

            }

            //重置页号为1
            this.searchMap.pageNo = 1;

            this.search();
        },
        //添加过滤条件
        addSearchItem: function(key, value){
            if ("category" == key || "brand" == key || "price" == key) {
                //参数1：要设置的对象，参数2：设置的属性，参数3：属性对应的值
                this.$set(this.searchMap, key, value);
            } else {
                //参数1：要设置的对象，参数2：设置的属性，参数3：属性对应的值
                this.$set(this.searchMap.spec, key, value);
            }

            //重置页号为1
            this.searchMap.pageNo = 1;

            this.search();
        },
        //根据关键字搜索
        search:function () {
            axios.post("itemSearch/search.do", this.searchMap).then(function (response) {
                app.resultMap = response.data;

                //构建分页导航条
                app.buildPageInfo();
            });
        },
        //构建分页导航条
        buildPageInfo: function () {
            this.pageNoList = [];

            //起始页号
            var startPageNo = 1;
            //结束页号
            var endPageNo = this.resultMap.totalPages;

            //要显示的页号数
            var totalShowPages = 5;

            // - 总页数大于要显示的总页号数则全部页号需要显示
            if(this.resultMap.totalPages > totalShowPages){

                //当前页号左右间隔数；整除取下限
                var interval = Math.floor(totalShowPages/2);

                //   - 起始页号 = 当前页号 - （要显示的页号数/2）
                startPageNo = this.searchMap.pageNo - interval;
                //   - 结束页号 = 当前页号 + （要显示的页号数/2）
                endPageNo = this.searchMap.pageNo + interval;

                if(startPageNo < 1){
                    //     - 如果起始页号小于1的时候则要设置为1；结束页号则为要显示的总页数
                    startPageNo = 1;
                    endPageNo = totalShowPages;
                } else {
                    //     - 如果结束页号大于总页数则设置为总页数；起始页号则为总页数-要显示的总页数加1
                    if(endPageNo > this.resultMap.totalPages){
                        endPageNo = this.resultMap.totalPages;
                        startPageNo = endPageNo - totalShowPages + 1;
                    }
                }
            }

            this.frontDot = false;
            if (startPageNo > 1) {
                this.frontDot = true;
            }

            this.backDot = false;
            if (endPageNo < this.resultMap.totalPages) {
                this.backDot = true;
            }


            for (let i = startPageNo; i <= endPageNo; i++) {
                this.pageNoList.push(i);
            }

        },
        //根据参数名字获取参数
        getParameterByName: function (name) {
            return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null
        }
    },
    created(){
        //获取搜索关键字
        this.searchMap.keywords = this.getParameterByName("keywords");

        this.search();
    }
});