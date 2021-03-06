import * as angular 
    from 'angular';

import { Directive, NgModule } 
    from '@angular/core';

import { downgradeComponent, UpgradeModule } 
    from '@angular/upgrade/static';

//User generated
import { DeactivateProfileComponent } 
    from './deactivateProfile.component.ts';

import { CommonNg2Module }
    from './../common/common.ts';

// This is the Angular 1 part of the module
export const DeactivateProfileModule = angular.module(
    'DeactivateProfileModule', 
    []
);

// This is the Angular 2 part of the module
@NgModule(
    {
        imports: [
            CommonNg2Module
        ],
        declarations: [ 
            DeactivateProfileComponent
        ],
        entryComponents: [ 
            DeactivateProfileComponent 
        ],
        providers: [
            
        ]
    }
)
export class DeactivateProfileNg2Module {}

// components migrated to angular 2 should be downgraded here
//Must convert as much as possible of our code to directives
DeactivateProfileModule.directive(
    'deactivateProfileNg2', 
    <any>downgradeComponent(
        {
            component: DeactivateProfileComponent,
        }
    )
);
