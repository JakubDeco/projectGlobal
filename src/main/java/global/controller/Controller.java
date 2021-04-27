package global.controller;

import global.database.Database;
import global.entity.Sensor;
import global.json.Json;
import global.log.Log;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class Controller {
    Database dat = new Database();
    Log log = new Log();
    Json j = new Json();

    private final ResponseEntity.BodyBuilder ok = ResponseEntity.status(200);
    private final ResponseEntity.BodyBuilder badRequest = ResponseEntity.status(400);
    private final ResponseEntity.BodyBuilder notFound = ResponseEntity.status(404);
    private final ResponseEntity.BodyBuilder dbsError = ResponseEntity.status(502);

    @GetMapping("/v1/get_general_stats")
    public ResponseEntity<String> getGeneral(){
        JSONObject object = j.getGeneralData(dat.getGeneralData());
        return ok.contentType(MediaType.APPLICATION_JSON).body(object.toJSONString());
    }

    @GetMapping(value = "/v1/get_latest", params = "sensor")
    public ResponseEntity<String> getDataBySensor(@RequestParam(value = "sensor") String sensorType){
        List<Sensor> list = dat.getDataBySensorType(sensorType);
        if (list.isEmpty()){
            log.error("Empty database");
            return badRequest.body("Empty database");
        }
        JSONObject object = j.getBySensorType(list);
        log.ok("Completed");
        return ok.contentType(MediaType.APPLICATION_JSON).body(object.toJSONString());
    }

    @PostMapping(value = "/v1/post_rainfall")
    public ResponseEntity<String> postRainfall(@RequestBody String body){
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(body);

            if (jsonObject.isEmpty()){
                log.error("Empty request body.");
                return badRequest.body("Empty request body.");
            }else if (jsonObject.get("sensor_type") == null
                    || !jsonObject.get("sensor_type").equals("rainfall")
                    || jsonObject.get("sensor_unit") == null
                    || !jsonObject.get("sensor_unit").equals("bucket")
                    || jsonObject.get("sensor_value") == null
                    || !jsonObject.get("sensor_value").equals(1.0)){
                log.error("Incorrect request body. 1");
                return badRequest.body("Incorrect request body.");
            }
            String sensor_type = (String) jsonObject.get("sensor_type");
            String sensor_unit = (String) jsonObject.get("sensor_unit");
            float sensor_value =  Float.parseFloat(String.valueOf(jsonObject.get("sensor_value")));

            if (dat.insertRecord(new Sensor(sensor_type,sensor_unit,sensor_value))){
                log.ok("post_rainfall successful");
                return ok.body("Measurement recorded.");
            }else {
                log.error("Database insert failed.");
                return dbsError.body("Database insert failed.");
            }


        } catch (ParseException e) {
            log.error(e.toString());
            return badRequest.body("Incorrect request body.");

        }
    }
    //// copied
    @PostMapping(value = "/v1/post_temperature")
    public ResponseEntity<String> postTemperature(@RequestBody String body){
        try {
            JSONObject jsonObject = (JSONObject) new JSONParser().parse(body);

            if (jsonObject.isEmpty()){
                log.error("Empty request body.");
                return badRequest.body("Empty request body.");
            }else if (jsonObject.get("sensor_type") == null
                    || !jsonObject.get("sensor_type").equals("temperature")
                    || jsonObject.get("sensor_unit") == null
                    || !jsonObject.get("sensor_unit").equals("celsius")
                    || jsonObject.get("sensor_value") == null
                    || !(jsonObject.get("sensor_value") instanceof Number) ){
                log.error("Incorrect request body. 1");
                return badRequest.body("Incorrect request body.");
            }
            String sensor_type = (String) jsonObject.get("sensor_type");
            String sensor_unit = (String) jsonObject.get("sensor_unit");
            float sensor_value =  Float.parseFloat(String.valueOf(jsonObject.get("sensor_value")));

            if (dat.insertRecord(new Sensor(sensor_type,sensor_unit,sensor_value))){
                log.ok("post_temperature successful");
                return ok.body("Measurement recorded.");
            }else {
                log.error("Database insert failed.");
                return dbsError.body("Database insert failed.");
            }


        } catch (ParseException | NumberFormatException e) {
            log.error(e.toString());
            return badRequest.body("Incorrect request body.");

        }
    }
    ////
}
