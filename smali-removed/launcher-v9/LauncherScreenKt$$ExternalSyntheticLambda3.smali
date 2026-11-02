.class public final synthetic Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;
.super Ljava/lang/Object;
.source "D8$$SyntheticClass"

# interfaces
.implements Lkotlin/jvm/functions/Function2;


# instance fields
.field public final synthetic f$0:Z

.field public final synthetic f$1:Ljava/lang/String;

.field public final synthetic f$2:Ljava/lang/String;

.field public final synthetic f$3:Ljava/lang/String;

.field public final synthetic f$4:Lkotlin/jvm/functions/Function0;

.field public final synthetic f$5:Lkotlin/jvm/functions/Function0;

.field public final synthetic f$6:I


# direct methods
.method public synthetic constructor <init>(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;I)V
    .registers 8

    .line 0
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-boolean p1, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$0:Z

    iput-object p2, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$1:Ljava/lang/String;

    iput-object p3, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$2:Ljava/lang/String;

    iput-object p4, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$3:Ljava/lang/String;

    iput-object p5, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$4:Lkotlin/jvm/functions/Function0;

    iput-object p6, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$5:Lkotlin/jvm/functions/Function0;

    iput p7, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$6:I

    return-void
.end method


# virtual methods
.method public final invoke(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .registers 12

    .line 0
    iget-boolean v0, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$0:Z

    iget-object v1, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$1:Ljava/lang/String;

    iget-object v2, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$2:Ljava/lang/String;

    iget-object v3, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$3:Ljava/lang/String;

    iget-object v4, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$4:Lkotlin/jvm/functions/Function0;

    iget-object v5, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$5:Lkotlin/jvm/functions/Function0;

    iget v6, p0, Lcom/hextech/aram/LauncherScreenKt$$ExternalSyntheticLambda3;->f$6:I

    move-object v7, p1

    check-cast v7, Landroidx/compose/runtime/Composer;

    check-cast p2, Ljava/lang/Integer;

    invoke-virtual {p2}, Ljava/lang/Integer;->intValue()I

    move-result v8

    invoke-static/range {v0 .. v8}, Lcom/hextech/aram/LauncherScreenKt;->$r8$lambda$VZ-vRFRoeiKPwry4FU4gKmjTSYA(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function0;ILandroidx/compose/runtime/Composer;I)Lkotlin/Unit;

    move-result-object p1

    return-object p1
.end method
