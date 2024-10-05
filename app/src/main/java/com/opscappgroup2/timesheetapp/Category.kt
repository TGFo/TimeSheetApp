package com.opscappgroup2.timesheetapp

data class Category(var name : String, var description : String) {
    public fun GetName() : String
    {
        return name
    }
    public fun GetDescription() : String
    {
        return description
    }
    public fun SetName(name: String)
    {
        this.name = name
    }
    public fun SetDescription(description: String)
    {
        this.description = description
    }
}