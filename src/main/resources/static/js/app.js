"use strict";

const icons = document.querySelectorAll('.icon-option');
const iconModal = document.querySelector('#iconModal');
const previewImg = document.querySelector('#preview');
const iconId = document.querySelector('#iconId');

icons.forEach((icon) => {
  icon.addEventListener('click', () => {
    previewImg.src = icon.dataset.path;
    iconId.value = icon.dataset.id;

    bootstrap.Modal.getOrCreateInstance(iconModal).hide();
  })
})