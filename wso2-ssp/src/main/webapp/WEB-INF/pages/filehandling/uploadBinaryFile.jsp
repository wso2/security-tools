<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <jsp:include page="../general/header.jsp"/>
</head>
<body class="hold-transition skin-blue sidebar-mini">
<div class="wrapper">

    <%--Mainheader is added--%>
    <jsp:include page="../general/mainHeader.jsp"/>


    <jsp:include page="../general/aside.jsp"/>


    <!-- Content Wrapper. Contains page content -->
    <div class="content-wrapper">
        <!-- Content Header (Page header) -->
        <section class="content-header">

        </section>

        <!-- Main content -->
        <section class="content">
            <div class="row">
                            <!-- /.col-->
                            <div class="col-md-10">
                                <div class="box box-info">
                                    <div class="box-header">
                                        <h3 class="box-title">Complete the following information before uploading the binary</h3>
                                    </div>
                                    <!-- /.box-header -->
                                    <div class="box-body pad">
                                        <form>
                                            <div class="form-group">
                                                <label> Developer Name: </label>
                                                <input type="text" id="username" class="form-control">
                                            </div>
                                            <div class="form-group">
                                                <label> Team </label>
                                                <input type="text" id="team" class="form-control">
                                            </div>
                                            <div class="form-group">
                                                <label> email address: </label>
                                                <input type="text" id="email" class="form-control">
                                            </div>
                                             <div class="form-group">
                                                 <label> Component Name: </label>
                                                 <input type="text" id="componentname" class="form-control">
                                             </div>
                                            </br>
                                            <div>
                                                <table class="table table-bordered text-center" style="border: none">
                                                    <tbody>
                                                    <tr>
                                                        <td class="col-md-10" style="border: none"></td>
                                                        <td style="border: none">
                                                            <button class="btn btn-block btn-success btn" id="submit" value="Submit">add details</button>
                                                        </td>
                                                    </tr>
                                                    </tbody>
                                                </table>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                                <!-- /.box -->
                            </div>
                            <!-- /.col-->
                <div class="col-md-10">
                    <div class="box box-info">
                        <div class="box-header" id="instructionheader" style="display: none">
                            <h3 class="box-title">Upload Client Details</h3>
                        </div>
                        <!-- /.box-header -->
                        <div class="box-body pad" id="fileupload" style="display: none" >
                            <form action="component/upload-binaryfile" method="post" enctype="multipart/form-data"
                                  modelAttribute="uploadedFile" action="#">
                                <div class="box-body">
                                    <div class="form-group">
                                        <label for="exampleInputFile">Upload Component</label>
                                        <input type="file" name="file" id="exampleInputFile">
                                    </div>

                                    <p></p>
                                    <table>
                                        <tr>
                                            <td style="width:540px"></td>
                                            <td style="width:120px"><button class="btn btn-block btn-success btn">Upload</button>
                                        </tr>
                                    </table>
                                </div>

                                <!-- /.box-body -->
                            </form>
                        </div>
                    </div>
                    <!-- /.box -->
                </div>
            </div>
            <!-- ./row -->
        </section>
        <!-- /.content -->
        <!-- /.content -->
    </div>
    <!-- /.content-wrapper -->

        <%--footer is added--%>
        <jsp:include page="../general/mainfooter.jsp"/>

    <!-- Control Sidebar -->
    <aside class="control-sidebar control-sidebar-dark">
        <!-- Create the tabs -->
        <ul class="nav nav-tabs nav-justified control-sidebar-tabs">
            <li class="active"><a href="#control-sidebar-home-tab" data-toggle="tab"><i class="fa fa-home"></i></a></li>
            <li><a href="#control-sidebar-settings-tab" data-toggle="tab"><i class="fa fa-gears"></i></a></li>
        </ul>
        <!-- Tab panes -->
        <div class="tab-content">
            <!-- Home tab content -->
            <div class="tab-pane active" id="control-sidebar-home-tab">
                <h3 class="control-sidebar-heading">Recent Activity</h3>
                <ul class="control-sidebar-menu">
                    <li>
                        <a href="javascript::;">
                            <i class="menu-icon fa fa-birthday-cake bg-red"></i>

                            <div class="menu-info">
                                <h4 class="control-sidebar-subheading">Langdon's Birthday</h4>

                                <p>Will be 23 on April 24th</p>
                            </div>
                        </a>
                    </li>
                </ul>
                <!-- /.control-sidebar-menu -->

                <h3 class="control-sidebar-heading">Tasks Progress</h3>
                <ul class="control-sidebar-menu">
                    <li>
                        <a href="javascript::;">
                            <h4 class="control-sidebar-subheading">
                                Custom Template Design
                                <span class="label label-danger pull-right">70%</span>
                            </h4>

                            <div class="progress progress-xxs">
                                <div class="progress-bar progress-bar-danger" style="width: 70%"></div>
                            </div>
                        </a>
                    </li>
                </ul>
                <!-- /.control-sidebar-menu -->

            </div>
            <!-- /.tab-pane -->
            <!-- Stats tab content -->
            <div class="tab-pane" id="control-sidebar-stats-tab">Stats Tab Content</div>
            <!-- /.tab-pane -->
            <!-- Settings tab content -->
            <div class="tab-pane" id="control-sidebar-settings-tab">
                <form method="post">
                    <h3 class="control-sidebar-heading">General Settings</h3>

                    <div class="form-group">
                        <label class="control-sidebar-subheading">
                            Report panel usage
                            <input type="checkbox" class="pull-right" checked>
                        </label>

                        <p>
                            Some information about this general settings option
                        </p>
                    </div>
                    <!-- /.form-group -->
                </form>
            </div>
            <!-- /.tab-pane -->
        </div>
    </aside>
    <!-- /.control-sidebar -->
    <!-- Add the sidebar's background. This div must be placed
         immediately after the control sidebar -->
    <div class="control-sidebar-bg"></div>
</div>
<!-- ./wrapper -->

<!-- REQUIRED JS SCRIPTS -->


<!-- jQuery 2.1.4 -->
<script src="resources/plugins/jQuery/jQuery-2.1.4.min.js"></script>
<!-- Bootstrap 3.3.5 -->
<script src="resources/bootstrap/js/bootstrap.min.js"></script>
<!-- FastClick -->
<script src="resources/plugins/fastclick/fastclick.min.js"></script>
<!-- AdminLTE App -->
<script src="resources/dist/js/app.min.js"></script>
<!-- AdminLTE for demo purposes -->
<script src="resources/dist/js/demo.js"></script>
<!-- CK Editor -->
<script src="https://cdn.ckeditor.com/4.4.3/standard/ckeditor.js"></script>
<!-- date-range-picker -->
<script src="https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.10.2/moment.min.js"></script>
<script src="resources/plugins/daterangepicker/daterangepicker.js"></script>
<!-- iCheck 1.0.1 -->
<script src="resources/plugins/iCheck/icheck.min.js"></script>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.11.0/jquery.min.js"></script>


<script>
        $(document).ready(function(){
        $("#submit").click(function(){
        var username = $("#username").val();
        var email = $("#email").val();
        var team = $("#team").val();
        var compname = $("#compname").val();

        // Returns successful data submission message when the entered information is stored in database.
        var dataString = 'username='+ username + '&email='+ email + '&team='+ team + '&componentname='+ componentname;
        if(username==''||email==''||team==''||componentname=='')
        {
            alert("Please Fill All Fields");
        }
        else
        {
            $.ajax({
                    type: "POST",
                    url: "add-component-details",
                    data: dataString,
                    cache: false,
                    success: function(result){
                                        if(result == "success"){
                                                alert("Thanks for submiting the details, please upload the binary file");
                                                 $('#fileupload').show();
                                                 $('#instructionheader').show();
                                                }
                                               else
                                                $('#fileupload').hide();
                                                $('#instructionheader').hide();
                                            }
            });
        }
        return false;
        });
        });
</script>

</body>
</html>