package com.duoduo.phoneshop.controller;

import com.duoduo.phoneshop.entity.*;
import com.duoduo.phoneshop.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
/**
 * 管理员控制器
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private OrderService orderService;
    /**
     * 搜索用户 - AJAX接口
     */
    @PostMapping("/users/search")
    @ResponseBody
    public Map<String, Object> searchUsers(@RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer size,
                                           HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }

        try {
            List<User> allUsers;

            // 如果有搜索关键字，进行过滤
            if (keyword != null && !keyword.trim().isEmpty()) {
                allUsers = userService.searchUsers(keyword.trim());
            } else {
                allUsers = userService.getAllUsers();
            }

            // 计算分页
            int totalCount = allUsers.size();
            int totalPages = (int) Math.ceil((double) totalCount / size);
            int startIndex = (page - 1) * size;
            int endIndex = Math.min(startIndex + size, totalCount);

            List<User> pagedUsers;
            if (startIndex < totalCount) {
                pagedUsers = allUsers.subList(startIndex, endIndex);
            } else {
                pagedUsers = new ArrayList<>();
            }

            Map<String, Object> data = new HashMap<>();
            data.put("users", pagedUsers);
            data.put("totalCount", totalCount);
            data.put("totalPages", totalPages);
            data.put("currentPage", page);

            result.put("success", true);
            result.put("data", data);
        } catch (Exception e) {
            log.error("搜索用户失败", e);
            result.put("success", false);
            result.put("message", "查询失败，请重试");
        }

        return result;
    }

    /**
     * 导出用户数据
     */
    @PostMapping("/users/export")
    public void exportUsers(@RequestParam(required = false) String keyword,
                            HttpServletResponse response,
                            HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return;
        }

        try {
            List<User> users;

            if (keyword != null && !keyword.trim().isEmpty()) {
                users = userService.searchUsers(keyword.trim());
            } else {
                users = userService.getAllUsers();
            }

            // 设置响应头
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition", "attachment;filename=users_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv");

            // 输出CSV
            PrintWriter writer = response.getWriter();
            writer.println("ID,用户名,邮箱,手机号,真实姓名,余额,角色,状态,注册时间");

            for (User user : users) {
                writer.printf("%d,%s,%s,%s,%s,%.2f,%s,%s,%s%n",
                        user.getId(),
                        user.getUsern(),
                        user.getEmail(),
                        user.getPhone() != null ? user.getPhone() : "",
                        user.getRealN() != null ? user.getRealN() : "",
                        user.getBalance(),
                        user.getRole(),
                        user.getStatus() == 1 ? "正常" : "禁用",
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(user.getCreateTime())
                );
            }

            writer.flush();
        } catch (Exception e) {
            log.error("导出用户数据失败", e);
        }
    }
    /**
     * 管理员仪表板
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        // 获取统计数据
        List<User> users = userService.getAllUsers();
        List<Product> products = productService.getAllProducts();
        BigDecimal totalSales = orderService.getTotalSales();
        Integer todayOrders = orderService.getTodayOrderCount();
        Integer monthOrders = orderService.getMonthOrderCount();

        model.addAttribute("userCount", users.size());
        model.addAttribute("productCount", products.size());
        model.addAttribute("totalSales", totalSales != null ? totalSales : BigDecimal.ZERO);
        model.addAttribute("todayOrders", todayOrders != null ? todayOrders : 0);
        model.addAttribute("monthOrders", monthOrders != null ? monthOrders : 0);

        return "admin/dashboard";
    }

    /**
     * 客户管理页面
     */
    @GetMapping("/users")
    public String userList(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        // 新增时用一个空对象
        model.addAttribute("user", new User());

        return "admin/users";
    }

    /**
     * 添加用户页面
     */
    @Deprecated
    @GetMapping("/users/add")
    public String addUserPage(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        return "admin/user-add";
    }

    /**
     * 添加用户
     */
    @PostMapping("/users/add")
    @ResponseBody
    public Map<String, Object> addUser(@RequestParam String usern,
                                       @RequestParam String password,
                                       @RequestParam String email,
                                       @RequestParam String phone,
                                       @RequestParam String realN,
                                       @RequestParam(defaultValue = "0") BigDecimal balance,
                                       @RequestParam(defaultValue = "USER") String role,
                                       @RequestParam(defaultValue = "1") Integer status,
                                       HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }

        try {
            User user = new User();
            user.setUsern(usern);
            user.setPassword(password);
            user.setEmail(email);
            user.setPhone(phone);
            user.setRealN(realN);
            user.setBalance(balance);
            user.setRole(role);
            user.setStatus(status);

            if (userService.register(user)) {
                result.put("success", true);
                result.put("message", "用户添加成功");
            } else {
                result.put("success", false);
                result.put("message", "用户名已存在");
            }
        } catch (Exception e) {
            log.error("添加用户失败", e);
            result.put("success", false);
            result.put("message", "添加失败，请重试");
        }

        return result;
    }
    /**
     * 获取用户详情（用于编辑）- AJAX接口
     */
    @GetMapping("/users/get/{id}")
    @ResponseBody
    public Map<String, Object> getUser(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }

        try {
            User user = userService.getUserById(id);
            if (user == null) {
                result.put("success", false);
                result.put("message", "用户不存在");
                return result;
            }

            result.put("success", true);
            result.put("data", user);
        } catch (Exception e) {
            log.error("获取用户失败", e);
            result.put("success", false);
            result.put("message", "获取用户失败");
        }

        return result;
    }

    /**
     * 编辑用户页面
     */
    @Deprecated
    @GetMapping("/users/edit/{id}")
    public String editUserPage(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        User user = userService.getUserById(id);
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        // 编辑时塞入要回显的实体
        model.addAttribute("user", user);

        return "admin/users";
    }

    /**
     * 更新用户
     */
    @PostMapping("/users/update")
    @ResponseBody
    public Map<String, Object> updateUser(@RequestParam Long id,
                                          @RequestParam String usern,
                                          @RequestParam(required = false) String password,
                                          @RequestParam String email,
                                          @RequestParam String phone,
                                          @RequestParam String realN,
                                          @RequestParam BigDecimal balance,
                                          @RequestParam String role,
                                          @RequestParam Integer status,
                                          HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }

        try {
            // 验证用户ID不为空
            if (id == null) {
                result.put("success", false);
                result.put("message", "更新失败：用户ID不能为空");
                return result;
            }

            // 验证用户是否存在
            User existingUser = userService.getUserById(id);
            if (existingUser == null) {
                result.put("success", false);
                result.put("message", "更新失败：用户不存在");
                return result;
            }

            User user = new User();
            user.setId(id);
            user.setUsern(usern);
            // 如果密码为空，则不更新密码
            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(password);
            }
            user.setEmail(email);
            user.setPhone(phone);
            user.setRealN(realN);
            user.setBalance(balance);
            user.setRole(role);
            user.setStatus(status);

            if (userService.updateUser(user)) {
                result.put("success", true);
                result.put("message", "用户更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            log.error("更新用户失败", e);
            result.put("success", false);
            result.put("message", "更新失败，请重试");
        }

        return result;
    }

    /**
     * 删除用户
     */
    @PostMapping("/users/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteUser(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }

        try {
            if (userService.deleteUser(id)) {
                result.put("success", true);
                result.put("message", "用户删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            log.error("删除用户失败", e);
            result.put("success", false);
            result.put("message", "删除失败，请重试");
        }

        return result;
    }
    /**
     * 商品管理页面
     */
    /**
     * 商品管理页面（支持搜索）
     */
    @GetMapping("/products")
    public String productList(@RequestParam(required = false) String keyword,
                              Model model,
                              HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        List<Product> products;

        // 如果有搜索关键字，进行搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.searchProducts(keyword.trim());
            model.addAttribute("keyword", keyword);
        } else {
            products = productService.getAllProducts();
        }

        model.addAttribute("products", products);

        return "admin/products";
    }

    /**
     * 添加商品页面
     */
    @GetMapping("/products/add")
    public String addProductPage(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        return "admin/product-add";
    }

    /**
     * 添加商品
     */
    @PostMapping("/products/add")
    public String addProduct(@ModelAttribute Product product,
                             @RequestParam("imageFile") MultipartFile imageFile,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        try {
            // 处理图片上传
            if (!imageFile.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                String uploadDir = "upload/products/";
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }

                File uploadFile = new File(uploadDir + fileName);
                imageFile.transferTo(uploadFile);
                product.setImage("/upload/products/" + fileName);
            }

            if (productService.addProduct(product)) {
                redirectAttributes.addFlashAttribute("success", "商品添加成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "添加失败");
            }
        } catch (Exception e) {
            log.error("添加商品失败", e);
            redirectAttributes.addFlashAttribute("error", "添加失败，请重试");
        }

        return "redirect:/admin/products";
    }

    /**
     * 编辑商品页面
     */
    @GetMapping("/products/edit/{id}")
    public String editProductPage(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        Product product = productService.getProductById(id);
        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("product", product);
        model.addAttribute("categories", categories);

        return "admin/product-edit";
    }

    /**
     * 更新商品
     */
    @PostMapping("/products/update")
    public String updateProduct(@ModelAttribute Product product,
                                @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        try {
            // 处理图片上传
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
                String uploadDir = "upload/products/";
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }

                File uploadFile = new File(uploadDir + fileName);
                imageFile.transferTo(uploadFile);
                product.setImage("/upload/products/" + fileName);
            }

            if (productService.updateProduct(product)) {
                redirectAttributes.addFlashAttribute("success", "商品更新成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "更新失败");
            }
        } catch (Exception e) {
            log.error("更新商品失败", e);
            redirectAttributes.addFlashAttribute("error", "更新失败，请重试");
        }

        return "redirect:/admin/products";
    }

    /**
     * 删除商品
     */
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        try {
            if (productService.deleteProduct(id)) {
                redirectAttributes.addFlashAttribute("success", "商品删除成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "删除失败");
            }
        } catch (Exception e) {
            log.error("删除商品失败", e);
            redirectAttributes.addFlashAttribute("error", "删除失败，请重试");
        }

        return "redirect:/admin/products";
    }

    /**
     * 分类管理页面
     */
    @GetMapping("/categories")
    public String categoryList(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        return "admin/categories";
    }

    /**
     * 添加分类
     */
    @PostMapping("/categories/add")
    @ResponseBody
    public Map<String, Object> addCategory(@RequestParam String name,
                                           @RequestParam String description,
                                           @RequestParam Integer sort,
                                           HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }

        try {
            Category category = new Category();
            category.setN(name);
            category.setDescription(description);
            category.setSort(sort);

            if (categoryService.addCategory(category)) {
                result.put("success", true);
                result.put("message", "添加成功");
            } else {
                result.put("success", false);
                result.put("message", "添加失败");
            }
        } catch (Exception e) {
            log.error("添加分类失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }

    /**
     * 更新分类
     */
    @PostMapping("/categories/update")
    @ResponseBody
    public Map<String, Object> updateCategory(@RequestParam Long id,
                                              @RequestParam String name,
                                              @RequestParam String description,
                                              @RequestParam Integer sort,
                                              HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }

        try {
            Category category = new Category();
            category.setId(id);
            category.setN(name);
            category.setDescription(description);
            category.setSort(sort);

            if (categoryService.updateCategory(category)) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            log.error("更新分类失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }

    /**
     * 删除分类
     */
    @PostMapping("/categories/delete/{id}")
    @ResponseBody
    public Map<String, Object> deleteCategory(@PathVariable Long id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            result.put("success", false);
            result.put("message", "无权限");
            return result;
        }

        try {
            if (categoryService.deleteCategory(id)) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "该分类下存在商品，不能删除");
            }
        } catch (Exception e) {
            log.error("删除分类失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }

    /**
     * 订单管理页面
     */
    @GetMapping("/orders")
    public String orderList(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);

        return "admin/orders";
    }

    /**
     * 发货
     */
    @PostMapping("/orders/deliver/{id}")
    public String deliverOrder(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        try {
            if (orderService.deliverOrder(id)) {
                redirectAttributes.addFlashAttribute("success", "发货成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "发货失败，请检查订单状态");
            }
        } catch (Exception e) {
            log.error("发货失败", e);
            redirectAttributes.addFlashAttribute("error", "发货失败，请重试");
        }

        return "redirect:/admin/orders";
    }

    /**
     * 删除订单
     */
    @PostMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        try {
            if (orderService.deleteOrder(id)) {
                redirectAttributes.addFlashAttribute("success", "订单删除成功");
            } else {
                redirectAttributes.addFlashAttribute("error", "该订单不能删除");
            }
        } catch (Exception e) {
            log.error("删除订单失败", e);
            redirectAttributes.addFlashAttribute("error", "删除失败，请重试");
        }

        return "redirect:/admin/orders";
    }

    /**
     * 报表页面
     */
    @GetMapping("/reports")
    public String reports(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/user/login";
        }

        // 获取各种统计数据
        List<Order> allOrders = orderService.getAllOrders();
        List<Order> pendingOrders = orderService.getOrdersByStatus(0);
        List<Order> paidOrders = orderService.getOrdersByStatus(1);
        List<Order> deliveredOrders = orderService.getOrdersByStatus(2);
        List<Order> completedOrders = orderService.getOrdersByStatus(3);

        model.addAttribute("totalOrders", allOrders.size());
        model.addAttribute("pendingOrders", pendingOrders.size());
        model.addAttribute("paidOrders", paidOrders.size());
        model.addAttribute("deliveredOrders", deliveredOrders.size());
        model.addAttribute("completedOrders", completedOrders.size());

        // 计算销售额
        BigDecimal totalSales = orderService.getTotalSales();
        model.addAttribute("totalSales", totalSales != null ? totalSales : BigDecimal.ZERO);

        return "admin/reports";
    }
}