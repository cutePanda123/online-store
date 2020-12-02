package com.imooc.seckill.controller;

import com.imooc.seckill.controller.viewmodel.GoodViewModel;
import com.imooc.seckill.error.BusinessException;
import com.imooc.seckill.response.CommonResponseType;
import com.imooc.seckill.service.GoodService;
import com.imooc.seckill.service.model.GoodModel;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller("good")
@RequestMapping("/good")
@CrossOrigin(allowCredentials = "true", origins = { "*" })
public class GoodController extends BaseController {
    @Autowired
    private GoodService goodService;

    @RequestMapping(value = "/create", method = { RequestMethod.POST }, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonResponseType createGood(@RequestParam(name = "title")String title,
                                         @RequestParam(name = "description")String description,
                                         @RequestParam(name = "price") BigDecimal price,
                                         @RequestParam(name = "stock")Integer stock,
                                         @RequestParam(name = "imageUrl")String imageUrl) throws BusinessException {
        GoodModel goodModel = new GoodModel();
        goodModel.setStock(stock);
        goodModel.setDescription(description);
        goodModel.setImageUrl(imageUrl);
        goodModel.setPrice(price);
        goodModel.setTitle(title);
        goodModel.setSales(0);

        GoodModel goodModelReturned = goodService.createGood(goodModel);
        GoodViewModel goodViewModel = convertViewModelFromDataModel(goodModelReturned);
        return CommonResponseType.newInstance(goodViewModel);
    }

    @RequestMapping(value = "/get/{id}", method = { RequestMethod.GET } )
    @ResponseBody
    public CommonResponseType getGood(@PathVariable(name = "id")Integer id) {
        GoodModel goodModel = goodService.getGoodById(id);
        GoodViewModel goodViewModel = convertViewModelFromDataModel(goodModel);
        return CommonResponseType.newInstance(goodViewModel);
    }

    @RequestMapping(value = "/list", method = { RequestMethod.GET })
    @ResponseBody
    CommonResponseType listGoods() {
        List<GoodModel> goodModels = goodService.listGoods();
        List<GoodViewModel> goodViewModels = goodModels.stream().map(goodModel -> {
            GoodViewModel goodViewModel = convertViewModelFromDataModel(goodModel);
            return goodViewModel;
        }).collect(Collectors.toList());
        return CommonResponseType.newInstance(goodViewModels);
    }

    private GoodViewModel convertViewModelFromDataModel(GoodModel goodModel) {
        if (goodModel == null) {
            return null;
        }
        GoodViewModel goodViewModel = new GoodViewModel();
        BeanUtils.copyProperties(goodModel, goodViewModel);

        if (goodModel.getEventModel() != null) {
            goodViewModel.setEventStatus(goodModel.getEventModel().getStatus());
            goodViewModel.setEventId(goodModel.getEventModel().getId());
            goodViewModel.setEventPrice(goodModel.getEventModel().getDealPrice());
            goodViewModel.setEventStartDate(goodModel.getEventModel().getStartDate().toString(DateTimeFormat.forPattern("MM-dd-yyyy HH:mm:ss")));
        } else {
            goodViewModel.setEventStatus(0);
        }
        return goodViewModel;
    }
}
