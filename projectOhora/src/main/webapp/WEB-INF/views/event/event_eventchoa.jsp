<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<div id="content">
	<div class="wrap">
		<div class="eventDetail">
			<div class="title-block"></div>
			<div class="event-wrap">
				<p style="text-align: center;">
					<img src="${contextPath}/resources/images/event_choa_image/1.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/2.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/3.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/4.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/5.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/6.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/7.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/8.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/9.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/10.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/11.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/12.jpg"
						alt=""> <img
						src="${contextPath}/resources/images/event_choa_image/13.jpg"
						alt="">

					<!-- go_top 버튼 -->
				<div id="floating">
					<div class="go_top">
						<span class="icon"></span>
					</div>
				</div>

				<script>
                // go_top 버튼 스크립트
                $(document).ready(function() {
                    // go_top 버튼 클릭 시 상단으로 부드럽게 이동
                    $('.go_top').on('click', function() {
                        $('html, body').animate({ scrollTop: 0 }, 400);
                        return false;
                    });
                    
                    // 스크롤 이벤트에 따라 go_top 버튼 표시
                    $(window).scroll(function() {
                        if ($(this).scrollTop() > 200) {
                            $('#floating').fadeIn();
                        } else {
                            $('#floating').fadeOut();
                        }
                    });
                });
                </script>
				</p>
			</div>
		</div>
	</div>
</div>