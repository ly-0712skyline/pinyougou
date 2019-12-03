var app = new Vue({
    el:"#app",
    data: {
        username:"",
        //支付日志或者交易编号
        outTradeNo: "",
        //支付总金额
        totalFee:0
    },
    methods: {
        //查询支付状态
        queryPayStatus: function (outTradeNo) {
            axios.get("pay/queryPayStatus.do?outTradeNo=" + outTradeNo + "&r="+Math.random()).then(function (response) {
                if(response.data.success){
                    //支付成功跳转到支付成功页面
                    location.href = "paysuccess.html?totalFee=" + app.totalFee;
                } else {
                    if("支付超时"==response.data.message){
                        //重新生成二维码
                        //alert(response.data.message);
                        app.createNative();
                    } else {
                        //跳转到支付失败
                        location.href = "payfail.html";
                    }
                }
            });
        },
        //生成支付二维码
        createNative: function () {
            //1、接收参数
            this.outTradeNo = this.getParameterByName("outTradeNo");
            //2、发送请求
            axios.get("pay/createNative.do?outTradeNo=" + this.outTradeNo + "&r=" + Math.random()).then(function (response) {
                if ("SUCCESS" == response.data.result_code) {
                    //下单成功
                    app.totalFee = (response.data.totalFee/100);
                    //3、生成二维码
                    var qr = new QRious({
                        //要渲染生成二维码图片的元素
                        element: document.getElementById("qrious"),
                        //大小
                        size: 250,
                        //级别
                        level: "Q",
                        //值
                        value: response.data.code_url
                    });
                    //查询支付状态
                    app.queryPayStatus(app.outTradeNo);
                } else {
                    alert("生成二维码失败！");
                }
            });


        },
        //获取用户名
        getUsername: function () {
            axios.get("cart/getUsername.do").then(function (response) {
                app.username = response.data.username;
            });
        },
        //根据参数名字获取参数
        getParameterByName: function (name) {
            return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.href) || [, ""])[1].replace(/\+/g, '%20')) || null
        }
    },
    created(){
        this.getUsername();
        //生成支付二维码
        this.createNative();
    }
});