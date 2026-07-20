with open("D:/work/proj2/tmp/add_company_query.sql","w",encoding="utf8") as f:
    meta = "{\"properties\":{},\"id\":\"test_company_query\",\"script\":\"select * from biz_insurance_company where id = #{id}\",\"groupId\":\"0\",\"name\":\"\u6d4b\u8bd5\u516c\u53f8\u67e5\u8be2\",\"createTime\":0,\"updateTime\":0,\"path\":\"/test/company\",\"method\":\"GET\",\"parameters\":[{\"name\":\"id\",\"required\":true}],\"options\":[],\"requestBody\":null,\"headers\":[],\"paths\":[],\"responseBody\":null,\"description\":null,\"requestBodyDefinition\":null,\"responseBodyDefinition\":null}"
    script = "select * from biz_insurance_company where id = #{id}"
    content = meta + "\n================================" + "\n" + script
    escaped = content.replace("'","''")
    f.write("USE ry_vue;\n")
    f.write("INSERT IGNORE INTO magic_api_file (file_path, file_content) VALUES ('magic-api/test/test_company.ms', '" + escaped + "');\n")
print("Done")
