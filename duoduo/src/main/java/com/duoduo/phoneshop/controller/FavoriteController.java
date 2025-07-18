package com.duoduo.phoneshop.controller;

import com.duoduo.phoneshop.entity.Favorite;
import com.duoduo.phoneshop.entity.User;
import com.duoduo.phoneshop.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏控制器
 *
 * @author DuoDuo
 * @date 2025/01/14
 */
@Slf4j
@Controller
@RequestMapping("/favorite")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 收藏列表页面
     */
    @GetMapping("")
    public String favoriteList(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        List<Favorite> favorites = favoriteService.getUserFavorites(currentUser.getId());
        model.addAttribute("favorites", favorites);

        return "user/favorites";
    }

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    @ResponseBody
    public Map<String, Object> addFavorite(@RequestParam Long productId,
                                           HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            if (favoriteService.addFavorite(currentUser.getId(), productId)) {
                result.put("success", true);
                result.put("message", "收藏成功");
            } else {
                result.put("success", false);
                result.put("message", "该商品已收藏");
            }
        } catch (Exception e) {
            log.error("添加收藏失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }

    /**
     * 取消收藏
     */
    @PostMapping("/remove")
    @ResponseBody
    public Map<String, Object> removeFavorite(@RequestParam Long productId,
                                              HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        try {
            if (favoriteService.removeFavorite(currentUser.getId(), productId)) {
                result.put("success", true);
                result.put("message", "取消收藏成功");
            } else {
                result.put("success", false);
                result.put("message", "取消失败");
            }
        } catch (Exception e) {
            log.error("取消收藏失败", e);
            result.put("success", false);
            result.put("message", "操作失败");
        }

        return result;
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    @ResponseBody
    public Map<String, Object> checkFavorite(@RequestParam Long productId,
                                             HttpSession session) {
        Map<String, Object> result = new HashMap<>();

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            result.put("success", false);
            result.put("isFavorite", false);
            return result;
        }

        try {
            boolean isFavorite = favoriteService.isFavorite(currentUser.getId(), productId);
            result.put("success", true);
            result.put("isFavorite", isFavorite);
        } catch (Exception e) {
            log.error("检查收藏状态失败", e);
            result.put("success", false);
            result.put("isFavorite", false);
        }

        return result;
    }

    /**
     * 清空收藏
     */
    @PostMapping("/clear")
    public String clearFavorites(HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/user/login";
        }

        try {
            if (favoriteService.clearFavorites(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("success", "收藏已清空");
            } else {
                redirectAttributes.addFlashAttribute("error", "清空失败");
            }
        } catch (Exception e) {
            log.error("清空收藏失败", e);
            redirectAttributes.addFlashAttribute("error", "操作失败");
        }

        return "redirect:/favorite";
    }

    /**
     * 获取收藏数量
     */
    @GetMapping("/count")
    @ResponseBody
    public Integer getFavoriteCount(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return 0;
        }

        return favoriteService.getFavoriteCount(currentUser.getId());
    }
}