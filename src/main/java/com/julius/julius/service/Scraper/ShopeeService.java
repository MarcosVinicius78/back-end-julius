package com.julius.julius.service.Scraper;

import java.math.BigInteger;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Service;

// import org.apache.http.HttpResponse;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.HttpResponse;

@Service
public class ShopeeService {

    public void getAuthShopee() {

        try {

            long timest = System.currentTimeMillis() / 1000L;
            String host = "https://partner.shopeemobile.com";
            String path = "/api/v2/shop/auth_partner";
            String redirect_url = "https://www.baidu.com/";
            long partner_id = 18199430003L;
            String tmp_partner_key = "...";
            String tmp_base_string = String.format("%s%s%s", partner_id, path, timest);
            byte[] partner_key;
            byte[] base_string;
            String sign = "";
            try {
                base_string = tmp_base_string.getBytes("UTF-8");
                partner_key = tmp_partner_key.getBytes("UTF-8");
                Mac mac = Mac.getInstance("HmacSHA256");
                SecretKeySpec secret_key = new SecretKeySpec(partner_key, "HmacSHA256");
                mac.init(secret_key);
                sign = String.format("%064x", new BigInteger(1, mac.doFinal(base_string)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            String url = host + path
                    + String.format("?partner_id=%s&timestamp=%s&sign=%s&redirect=%s", partner_id, timest,
                            sign, redirect_url);
            System.out.println(url);

            String access_token = "HB6T6RCEXPLBYXMT4ZGDD2PMUZE5DGII";

            Unirest.setTimeouts(0, 0);
            HttpResponse<String> response = Unirest.get(
            "https://partner.shopeemobile.com/api/v2/product/get_item_base_info?"+"timestamp="+timest+"&"+"sign="+sign+"&"+"shop_id="+"818390117"+"&"+"need_tax_info="+"true"+"&"+"item_id_list="+"18424274024"+"&"+"partner_id=18199430003"+"&"+"need_complaint_policy="+"true"+"&"+"access_token="+access_token)
            .asString();

            System.out.println(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
