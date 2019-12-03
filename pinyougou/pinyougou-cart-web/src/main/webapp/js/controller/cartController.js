var app = new Vue({
    el:"#app",
    data: {
        username:"",
        //购物车列表
        cartList:[],
        //总价和总数量
        totalValue:{totalNum:0, totalMoney:0.0}
    },
    methods: {
        //加入购物车
        addItemToCartList:function(itemId, num){
            axios.get("cart/addItemToCartList.do?itemId="+itemId+"&num="+num).then(function (response) {
                if(response.data.success){
                    app.findCartList();
                } else {
                    alert(response.data.message);
                }
            });
        },
        //查询购物车数据
        findCartList:function(){
          axios.get("cart/findCartList.do").then(function (response) {
              app.cartList = response.data;

              //计算总数和总价
              app.totalValue = app.sumTotalValue(response.data);
          });
        },
        //计算总价和总数量
        sumTotalValue: function(cartList){
            var totalValue = {"totalNum":0, "totalMoney":0.0};
            for (let i = 0; i < cartList.length; i++) {
                const cart = cartList[i];
                for (let j = 0; j < cart.orderItemList.length; j++) {
                    const orderItem = cart.orderItemList[j];
                    totalValue.totalNum += orderItem.num;
                    totalValue.totalMoney += orderItem.totalFee;
                }
            }
            return totalValue;
        },
        //获取用户名
        getUsername: function () {
            axios.get("cart/getUsername.do").then(function (response) {
                app.username = response.data.username;
            });
        }
    },
    created(){
        this.getUsername();
        //查询购物车列表
        this.findCartList();
    }
});