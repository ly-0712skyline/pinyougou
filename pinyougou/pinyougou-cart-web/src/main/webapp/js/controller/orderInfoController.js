var app = new Vue({
    el:"#app",
    data: {
        username:"",
        //购物车列表
        cartList:[],
        //总价和总数量
        totalValue:{totalNum:0, totalMoney:0.0},
        //地址列表
        addressList:[],
        //当前选择的地址
        selectedAddress:{"id":0},
        //订单对象；默认微信支付
        order:{"paymentType":1}
    },
    methods: {
        //提交订单
        submitOrder:function(){
            //设置收件人地址到提交的订单对象中
            this.order.receiver = this.selectedAddress.contact;
            this.order.receiverMobile = this.selectedAddress.mobile;
            this.order.receiverAreaName = this.selectedAddress.address;
            axios.post("order/add.do", this.order).then(function (response) {
                if(response.data.success){
                    //如果是微信付款则跳转到支付页面
                    if (app.order.paymentType == 1) {
                        location.href = "pay.html?outTradeNo=" + response.data.message;
                    } else {
                        //如果是货到付款则跳转到支付成功页面
                        location.href="paysuccess.html";
                    }

                } else {
                    alert(response.data.message);
                }
            });
        },
        //选择地址
        selectAddress: function(address){
            this.selectedAddress = address;
        },
        //查询地址列表
        findAddressList: function () {
            axios.get("address/findAddressList.do").then(function (response) {
                app.addressList = response.data;

                //获取默认地址
                for (let i = 0; i < response.data.length; i++) {
                     const address = response.data[i];
                    if (address.isDefault == '1') {
                        app.selectedAddress = address;
                        break;
                    }

                }
            });
        },
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
        //加载当前登录用户的收件人地址
        this.findAddressList();
    }
});