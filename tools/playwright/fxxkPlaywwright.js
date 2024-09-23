(() => {
    let set = localStorage.getItem("settings");
    if (set) {
        set = JSON.parse(set);
        set.language = 'zh';
        set.server = 0;
    } else { 
        set = { language: 'zh', server: 0 }
    }
    localStorage.setItem("settings", JSON.stringify(set));
    let sd = localStorage.getItem("studentDisplay");
    if (sd) {
        sd = JSON.parse(sd);
        sd.WeaponLevelDisplay = 50;
        sd.BondLevelDisplay = 20;
    } else { 
        sd = { WeaponLevelDisplay: 50, BondLevelDisplay: 20 }
    }
    localStorage.setItem("studentDisplay", JSON.stringify(sd));
    window.setInterval(() => {
        const model = document.querySelectorAll(".show").length;
        if (model !== 0) {
            const btn = document.querySelector(".btn-close-white");
            if (btn) {
                btn.click();
            }
        }
    }, 1000);
})();