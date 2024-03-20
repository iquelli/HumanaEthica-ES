import { ISOtoString } from '@/services/ConvertDateService';
import Activity from '@/models/activity/Activity';
import Volunteer from '@/models/volunteer/Volunteer';

export default class Enrollment {
  id: number | null = null;
  volunteerName!: string;
  motivation!: string;
  participating!: boolean;
  enrollmentDateTime!: string;
  activity!: Activity;
  volunteer!: Volunteer;

  constructor(jsonObj?: Enrollment) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.volunteerName = jsonObj.volunteerName;
      this.motivation = jsonObj.motivation;
      this.participating = jsonObj.participating;
      this.enrollmentDateTime = ISOtoString(jsonObj.enrollmentDateTime);
      this.activity = jsonObj.activity;
      this.volunteer = jsonObj.volunteer;
    }
  }
}
