package com.france.service;

import com.france.domain.*;
import com.france.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ImagesRepository imagesRepository;
    private final VariantIdRepository variantIdRepository;
    private final OptionsRepository optionsRepository;
    private final VariantsRepository variantsRepository;

    private JSONArray imageJsonList, variantJsonList;
    private List<VariantId> variantIds;

    // insert
    public JSONArray saveList() throws ParseException {
        JSONArray jsonArray = parseList(getProductsFromAPI());
        List<Product> productList = getProductList(jsonArray);
        productRepository.saveAll(productList);
        imagesRepository.saveAll(getImageList(imageJsonList));
        optionsRepository.saveAll(getOptionList(jsonArray));
        variantsRepository.saveAll(getVariantList(variantJsonList));
        variantIdRepository.saveAll(variantIds);

        return jsonArray;
    }

    protected String getProductsFromAPI() {
        String authString = " e9260a4b38326b5f96c5bebb52f966be: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";
        byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
        //System.out.println(Arrays.toString(authEncBytes).toCharArray());
        String authStringEnc = new String(authEncBytes);
        URL url = null;

        try {
            url = new URL("https://first-homme.myshopify.com/admin/api/2021-01/products.json");
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("ContentType", "application/json");
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            InputStream is = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = br.readLine()) != null) {
                response.append(line + "\n");
            }

            return response + "";

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    protected JSONArray parseList(String response) throws ParseException {
        List<Product> list = new ArrayList<>();

        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(response);
        JSONArray jsonArray = (JSONArray) jsonObject.get("products");

        return jsonArray;
    }

    protected JSONArray getProductList(JSONArray jsonArray){
        imageJsonList = new JSONArray();
        variantJsonList = new JSONArray();
        JSONArray productList = new JSONArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            List<Image> imgList = new ArrayList<Image>();
            JSONObject product = (JSONObject) jsonArray.get(i);
            Product newProduct = Product.builder()
                    .id(Long.parseLong(product.get("id").toString()))
                    .title(product.get("title").toString())
                    .vendor(product.get("vendor").toString())
                    .createdAt(parseDate(product.get("created_at")))
                    .updatedAt(parseDate(product.get("updated_at")))
                    .productImagesList(new ArrayList<>())
                    .variantList(new ArrayList<>())
                    .build();
            if (product.get("images")!=null){getImageJsonList((JSONArray) product.get("images")); }
            if (product.get("variants")!=null){ getVariantJsonList((JSONArray) product.get("variants")); }
            productList.add(newProduct);
        }
        return productList;
    }

    protected List<Image> getImageList(JSONArray imageJsonList){
        List<Image> imageList = new ArrayList<Image>();
        for(int i = 0; i<imageJsonList.size(); i++){
            JSONObject image = (JSONObject) imageJsonList.get(i);
            Product product = productRepository.findById(Long.parseLong(image.get("product_id").toString())).get();
            Image newImage = Image.builder()
                    .imageId(Long.parseLong(image.get("id").toString()))
                    .position(image.get("position").toString())
                    .created_at(parseDate(image.get("created_at")))
                    .updated_at(parseDate(image.get("updated_at")))
                    .imagePath(image.get("src").toString())
                    .variantIds(new ArrayList<>())
                    .product(product)
                    .build();
            product.getProductImagesList().add(newImage);
            imageList.add(newImage);
        }
        return imageList;
    }

    protected List<Option> getOptionList(JSONArray jsonArray){
        List<Option> optionList = new ArrayList<Option>();
        for(int i =0; i<jsonArray.size(); i++) {
            JSONObject jsonProduct = (JSONObject) jsonArray.get(i);
            Long productId = Long.parseLong(jsonProduct.get("id").toString());
            Optional<Product> product = productRepository.findById(productId);
            JSONArray optionTotalList = ((JSONArray) jsonProduct.get("options"));
            for (int j = 0; j < optionTotalList.size(); j++) {
                JSONObject option = (JSONObject) optionTotalList.get(j);
                Option newOption = Option.builder()
                        .optionId(Long.parseLong(option.get("id").toString()))
                        .name(option.get("name").toString())
                        .position(option.get("position").toString())
                        .product(product.get())
                        .build();

                optionList.add(newOption);
            }
        }
        return optionList;
    }

    protected List<Variant> getVariantList(JSONArray variantTotalList){
        List<Variant> variantList = new ArrayList<>();
        variantIds = new ArrayList<>();
        for(int i=0; i<variantTotalList.size(); i++){
            JSONObject variant = (JSONObject) variantTotalList.get(i);
            Product product = productRepository.findById(Long.parseLong(variant.get("product_id").toString())).get();
            Variant newVariant = Variant.builder()
                        .variantId(Long.parseLong(variant.get("id").toString()))
                        .title(variant.get("title").toString())
                        .price(Double.parseDouble(variant.get("price").toString()))
                        .product(product)
                        .build();
            product.getVariantList().add(newVariant);

            if(variant.get("image_id")!=null){
                Image image = imagesRepository.findById(Long.parseLong(variant.get("image_id").toString())).get();
                VariantId variantId = VariantId.builder()
                        .variantId(Long.parseLong(variant.get("id").toString()))
                        .image(image)
                        .build();
                image.getVariantIds().add(variantId);
                newVariant.setImage_id(Long.parseLong(variant.get("id").toString()));
                variantIds.add(variantId);
            }
            variantList.add(newVariant);
        }
        return variantList;
    }

    protected LocalDateTime parseDate(Object object){
        LocalDateTime localDateTime = LocalDateTime.parse(object.toString().substring(0,19));
        return localDateTime;
    }

    protected void getImageJsonList(JSONArray jsonArray){
        for(int i = 0; i<jsonArray.size(); i++){
            JSONObject jsonObject= (JSONObject)jsonArray.get(i);
            imageJsonList.add(jsonObject);
        }
    }

    protected void getVariantJsonList(JSONArray jsonArray){
        for(int i = 0; i<jsonArray.size(); i++){
            JSONObject jsonObject= (JSONObject)jsonArray.get(i);
            variantJsonList.add(jsonObject);
        }
    }

}
