package com.duoduo.phoneshop.controller;

import com.duoduo.phoneshop.entity.Category;
import com.duoduo.phoneshop.entity.Product;
import com.duoduo.phoneshop.entity.User;
import com.duoduo.phoneshop.service.CategoryService;
import com.duoduo.phoneshop.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 首页控制器
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 首页
     */
    @GetMapping("/")
    public String index(Model model, HttpSession session) {
        // 获取所有分类
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        // 获取所有商品
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);

        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);

        return "index";
    }

    /**
     * 按分类查看商品
     */
    @GetMapping("/category/{id}")
    public String category(@PathVariable Long id, Model model, HttpSession session) {
        // 获取所有分类
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        // 获取当前分类
        Category currentCategory = categoryService.getCategoryById(id);
        model.addAttribute("currentCategory", currentCategory);

        // 获取该分类下的商品
        List<Product> products = productService.getProductsByCategory(id);
        model.addAttribute("products", products);

        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);

        return "index";
    }

    /**
     * 搜索商品
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword,
                         Model model, HttpSession session) {
        // 获取所有分类
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        // 搜索商品
        List<Product> products;
        if (keyword != null && !keyword.trim().isEmpty()) {
            products = productService.searchProducts(keyword);
            model.addAttribute("keyword", keyword);
        } else {
            products = productService.getAllProducts();
        }
        model.addAttribute("products", products);

        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);

        return "index";
    }

    /**
     * 商品详情
     */
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model, HttpSession session) {
        // 获取商品信息
        Product product = productService.getProductById(id);
        if (product == null) {
            return "redirect:/";
        }
        model.addAttribute("product", product);

        // 获取所有分类
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        // 获取当前登录用户
        User currentUser = (User) session.getAttribute("currentUser");
        model.addAttribute("currentUser", currentUser);

        return "product-detail";
    }
}