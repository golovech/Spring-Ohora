package kr.ohora.sl;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import kr.ohora.sl.domain.AdminPageCriteria;
import kr.ohora.sl.domain.AdminPageDTO;
import kr.ohora.sl.domain.OrderDetailDTO;
import kr.ohora.sl.domain.ProductDTO;
import kr.ohora.sl.domain.UserDTO;
import kr.ohora.sl.service.admin.AdminService;
import lombok.extern.log4j.Log4j;

@Controller
@Log4j
@RequestMapping("/admin/*")
public class AdminPageController {

	@Autowired
	AdminService adminService;

	@GetMapping("productDetail.htm")
	public String productDetail(@RequestParam("pdtid") int pdtid, Model model) throws SQLException {
		log.info("> AdminController.productDetail() ...");
		ProductDTO productDTO = this.adminService.getProductInfo(pdtid);
		model.addAttribute("productDTO", productDTO);
		return "/admin/productDetail";
	}

	// 페이징처리
	@GetMapping("productList.htm")
	public String productList(
			@RequestParam(value = "currentPage", defaultValue = "1") int currentPage,
			@RequestParam(value = "sort_method", required = false) Integer sortMethod,
			@RequestParam(value = "keyword", required = false) String keyword,
			Model model, AdminPageCriteria criteria) {
		log.info("> AdminController.productList() ...");

		criteria.setCurrentPage(currentPage);
		criteria.setSortMethod(sortMethod);
		criteria.setKeyword(keyword);
		criteria.setNumberOfPageBlock(10);
		criteria.setNumberPerPage(10); 

		try {
			ArrayList<ProductDTO> productDTO = this.adminService.getAllProductList(criteria);
			if (productDTO == null || productDTO.isEmpty()) {
				log.warn("error : ProductDTO list가 null 이거나 비어있음!!");
				productDTO = new ArrayList<>();
			}
			model.addAttribute("productDTO", productDTO);
		} catch (SQLException e) {
			log.error("Error while fetching product list", e);
		}

		int total = this.adminService.getTotalRecords(criteria);
		model.addAttribute("pdto", new AdminPageDTO(criteria, total));

		return "/admin/productList";
	}


	// 검색
	@GetMapping("productSearch.htm")
	public String productSearch(
			  @RequestParam("keyword") String keyword
			, @RequestParam("currentPage") int currentPage
			, Model model, AdminPageCriteria criteria) {
		log.info("> AdminController.productSearch() ...");
		System.out.println("> keyword : " + keyword);
		criteria.setKeyword(keyword);
		criteria.setCurrentPage(currentPage);
		criteria.setNumberOfPageBlock(10);
		criteria.setNumberPerPage(39);

		model.addAttribute("list", this.adminService.getProductsBySearch(criteria));

		int total = this.adminService.getTotalRecordsBySearch(criteria, keyword);
		model.addAttribute("pdto",new AdminPageDTO(criteria, total));

		return "/admin/productList";
	}

	// 뷰 용도
	@GetMapping("productReg.htm")
	public String productRegView(HttpSession session) {
		log.info("AdminController.productRegView() ...");
		return "/admin/productReg";
	}

	// 상품 등록
	@PostMapping("productReg.htm")
	public String productInsert(ProductDTO productDTO, HttpServletRequest request) 
	        throws ClassNotFoundException, SQLException, IllegalStateException, IOException {
	    log.info(">> productInsert() ... 상품 등록 시작~~~");

	    CommonsMultipartFile multipartFile = productDTO.getFile();
	    if (multipartFile == null || multipartFile.isEmpty()) {
	        log.error("파일이 없습니다!");
	        return "redirect:/admin/productReg.htm?error=file_missing";
	    }

	    log.info("파일 이름: " + multipartFile.getOriginalFilename());
	    String uploadRealPath = request.getServletContext().getRealPath("/resources/images/prd_image/imgs");

	    String originalFilename = multipartFile.getOriginalFilename();
	    String fileNameWithoutExtension = originalFilename.substring(0, originalFilename.lastIndexOf(".")); // 확장자 제거
	    String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 확장자 추출
	    
	    // 실제 저장 파일 경로 생성
	    File dest = new File(uploadRealPath, fileNameWithoutExtension + fileExtension);

	    try {
	        log.info("저장 파일 경로: " + dest.getAbsolutePath());
	        multipartFile.transferTo(dest); // 파일 저장
	        productDTO.setPdtimgurl(fileNameWithoutExtension); // DB에 확장자를 제외한 이름만 저장
	    } catch (IOException e) {
	        log.error("파일 업로드 실패: " + e.getMessage(), e);
	        return "redirect:/admin/productReg.htm?error=upload_fail";
	    }

	    try {
	        productDTO.calcDiscountAmount(); // 추가 데이터 처리
	        adminService.productInsert(productDTO); // DB에 상품 등록
	        log.info("> ********* 상품 등록 성공!!! ********* <");
	    } catch (Exception e) {
	        log.error("상품 등록 실패 .......", e);
	        return "redirect:/admin/productReg.htm?error=insert_fail";
	    }

	    return "redirect:/admin/productList.htm";
	}

	@GetMapping("customerList.htm")
	public String customerList(Model model) throws SQLException {
		log.info("> AdminPageController customerList()...");
		ArrayList<UserDTO> userDTO = this.adminService.getAllCustomerList();
		model.addAttribute("userDTO", userDTO);
		return "/admin/customerList";
	}

	@ResponseBody
	@RequestMapping(
			value = "/admin/customerDetail.ajax",
			method = RequestMethod.GET,
			produces = MediaType.APPLICATION_JSON_UTF8_VALUE
			)
	public ResponseEntity<UserDTO> customerDetail(@RequestParam("userid") Integer userid) {
		log.info("> AdminPageController customerDetail()... userid: " + userid);

		try {
			// 서비스 호출을 통해 회원 정보를 조회
			UserDTO userDetail = this.adminService.getCustomerDetailById(userid);

			if (userDetail == null) {
				// 회원 정보가 없을 경우 404 상태 반환
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
			}

			// 성공적으로 데이터를 반환
			return ResponseEntity.status(HttpStatus.OK).body(userDetail);
		} catch (Exception e) {
			log.error("Error retrieving customer details", e);
			// 예외 발생 시 500 상태 반환
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@ResponseBody
	@RequestMapping(value = "/admin/updateUserStatus.ajax", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
	public ResponseEntity<Map<String, String>> updateUserStatus(@RequestParam("userid") Integer userid,
	                                                            @RequestParam("enabled") String enabled) {
	    log.info("> AdminPageController updateUserStatus()... userid: " + userid + ", enabled: " + enabled);

	    Map<String, String> response = new HashMap<>();
	    try {
	        boolean isEnabled = Boolean.parseBoolean(enabled);  // 'true' 또는 'false'를 Boolean으로 변환
	        boolean result = adminService.modifyUserStatus(userid, isEnabled);
	        
	        if (result) {
	            response.put("message", "회원 정보가 수정되었습니다.");
	            return ResponseEntity.status(HttpStatus.OK).body(response); // 성공 시
	        } else {
	            response.put("message", "회원 정보 수정 실패");
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 실패 시
	        }
	    } catch (Exception e) {
	        log.error("Error updating user status", e);
	        response.put("message", "회원 정보 수정에 실패했습니다.");
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); // 예외 발생 시
	    }
	}
	
	@GetMapping("orderList.htm")
	public String orderList(Model model) throws Exception {
		ArrayList<OrderDetailDTO> ordList = adminService.getOrderList();
		model.addAttribute("ordList", ordList);
		return "/admin/orderList";
	}
	
	@GetMapping("orderListDetail.htm")
	public String orderListDetail() {
		return "/admin/orderListDetail";
	}

}
