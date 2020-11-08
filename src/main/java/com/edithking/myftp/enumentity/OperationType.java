package com.edithking.myftp.enumentity;

public enum OperationType {
    ADD(1,"新增"),
    DELETE(2,"删除"),
    UPDATE(3,"修改");

    public Integer typeId;
    public String name;

    OperationType(Integer typeId,String name){
        this.name = name;
        this.typeId = typeId;
    }

}
