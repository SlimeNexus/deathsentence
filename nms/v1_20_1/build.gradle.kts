plugins {
    id("io.papermc.paperweight.userdev")
}

dependencies {
    implementation(project(":nms:common"))
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
}
