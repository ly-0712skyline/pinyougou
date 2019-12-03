var app = new Vue({
    el: "#app",
    data: {
        entity: {"username": "", "password": "", "phone": ""},
        password:"",
        smsCode: ""
    },
    methods: {
        register: function () {
            if (this.entity.username == "") {
                alert("请输入用户名");
                return;
            }
            if (this.entity.password == "") {
                alert("请输入密码");
                return;
            }
            //判断两次密码是否一致
            if (this.password != this.entity.password) {
                alert("两次输入的密码不一致;请重新输入");
                return;
            }

            if (this.entity.phone == "") {
                alert("请输入手机号");
                return;
            }

            axios.post("user/add.do?smsCode=" + this.smsCode, this.entity).then(function (response) {
                alert(response.data.message);
            });
        },
        sendSmsCode: function () {
            if (this.entity.phone == null || this.entity.phone == "") {
                alert("请输入手机号");
                return;
            }

            axios.get("user/sendSmsCode.do?phone=" + this.entity.phone).then(function (response) {
                alert(response.data.message);
            });
        }
    }
});